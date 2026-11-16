package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.places.LocResp;
import com.rotacultural.rcservice.dtos.recommendations.RecommendationReq;
import com.rotacultural.rcservice.dtos.recommendations.RecommendationResp;
import com.rotacultural.rcservice.dtos.recommendations.RecommendedPlaceResp;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Preferences;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.PreferencesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RecommendationsService {

    private final PlacesRepository placesRepository;
    private final TripsRepository tripsRepository;
    private final PreferencesRepository preferencesRepository;
    private final PlacesService placesService;
    private final AuthService authService;

    public RecommendationsService(
            PlacesRepository placesRepository,
            TripsRepository tripsRepository,
            PreferencesRepository preferencesRepository,
            PlacesService placesService,
            AuthService authService
    ) {
        this.placesRepository = placesRepository;
        this.tripsRepository = tripsRepository;
        this.preferencesRepository = preferencesRepository;
        this.placesService = placesService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public RecommendationResp recommendForTrip(Long tripId) {
        Trips trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        List<Long> categoryIds = preferencesRepository.findByTrips_IdTrip(tripId)
                .stream()
                .map(Preferences::getCategories)
                .map(c -> c.getIdCat())
                .toList();

        int days = 1;
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            days = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        }

        return buildRecommendations(trip.getDestination(), categoryIds, trip.getBudget(), days);
    }

    @Transactional(readOnly = true)
    public RecommendationResp preview(RecommendationReq req) {
        int days = req.getDays() != null ? req.getDays() : 3;
        return buildRecommendations(req.getDestination(), req.getCategoryIds(), req.getBudget(), days);
    }

    @Transactional(readOnly = true)
    public List<Places> selectApprovedPlacesForTrip(Trips trip) {
        List<Long> categoryIds = preferencesRepository.findByTrips_IdTrip(trip.getIdTrip())
                .stream()
                .map(p -> p.getCategories().getIdCat())
                .toList();

        int days = 1;
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            days = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        }

        RecommendationResp resp = buildRecommendations(
                trip.getDestination(), categoryIds, trip.getBudget(), days
        );

        List<Long> ids = resp.getPlaces().stream()
                .map(item -> item.getPlace().getId())
                .limit(Math.max(days * 3L, 3L))
                .toList();

        return ids.stream()
                .map(id -> placesRepository.findById(id).orElse(null))
                .filter(p -> p != null && p.getStatus() == Status.APROVADO)
                .toList();
    }

    private RecommendationResp buildRecommendations(
            String destination,
            List<Long> categoryIds,
            BigDecimal budget,
            int days
    ) {
        List<Places> approvedPlaces = placesRepository.findByStatusOrderByDateCreateDesc(Status.APROVADO);
        List<String> destinationTokens = tokenize(destination);
        List<Long> safeCategoryIds = categoryIds != null ? categoryIds : List.of();

        BigDecimal dailyBudget = null;
        if (budget != null && days > 0) {
            dailyBudget = budget.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        }

        List<RecommendedPlaceResp> scored = new ArrayList<>();

        for (Places place : approvedPlaces) {
            if (!destinationTokens.isEmpty() && !matchesDestination(place, destinationTokens)) {
                continue;
            }

            boolean withinBudget = isWithinBudget(place, budget, dailyBudget);
            if (budget != null && !withinBudget) {
                continue;
            }

            boolean matchesPreference = place.getCategory() != null
                    && safeCategoryIds.contains(place.getCategory().getIdCat());

            LocResp locResp = placesService.toResponse(place);
            int score = 0;
            List<String> reasons = new ArrayList<>();

            if (!destinationTokens.isEmpty()) {
                if (matchesCity(place, destinationTokens)) {
                    score += 25;
                    reasons.add("Destino compatível (" + place.getCity() + ")");
                } else {
                    score += 12;
                    reasons.add("Mesma região (" + place.getState() + ")");
                }
            }

            if (matchesPreference) {
                score += 50;
                reasons.add("Combina com sua preferência cultural (" + locResp.getCategoryName() + ")");
            }

            if (withinBudget) {
                score += (budget != null ? 25 : 10);
                if (place.getPrice() == Price.GRATUITO) {
                    reasons.add("Entrada gratuita");
                } else if (budget != null) {
                    reasons.add("Dentro do orçamento informado");
                }
            }

            if (locResp.getAverageRating() != null && locResp.getAverageRating() > 0) {
                score += (int) Math.round(locResp.getAverageRating() * 2.0);
                reasons.add("Avaliação média " + locResp.getAverageRating());
            }

            if (reasons.isEmpty()) {
                reasons.add("Local cultural aprovado");
            }

            scored.add(new RecommendedPlaceResp(
                    locResp,
                    score,
                    matchesPreference,
                    withinBudget,
                    String.join(" • ", reasons)
            ));
        }

        scored.sort(Comparator.comparingInt(RecommendedPlaceResp::getScore).reversed()
                .thenComparing(item -> item.getPlace().getAverageRating() != null ? item.getPlace().getAverageRating() : 0.0,
                        Comparator.reverseOrder()));

        boolean sufficientData;
        String message;
        if (scored.isEmpty()) {
            sufficientData = false;
            message = "Não foram encontrados locais aprovados no banco de dados para o destino, preferências ou orçamento informados.";
        } else if (scored.size() < days) {
            sufficientData = false;
            message = "Encontrados " + scored.size() + " locais aprovados para este destino (quantidade menor que os "
                    + days + " dias da viagem). Nenhum dado fictício foi gerado.";
        } else {
            sufficientData = true;
            message = "Recomendações calculadas com base nos locais reais cadastrados e aprovados.";
        }

        return new RecommendationResp(
                destination,
                days,
                budget,
                sufficientData,
                message,
                scored
        );
    }

    private boolean matchesDestination(Places place, List<String> tokens) {
        return matchesCity(place, tokens) || matchesState(place, tokens);
    }

    private boolean matchesCity(Places place, List<String> tokens) {
        String city = normalize(place.getCity());
        if (city.isEmpty()) {
            return false;
        }
        return tokens.stream().anyMatch(token -> city.contains(token) || token.contains(city));
    }

    private boolean matchesState(Places place, List<String> tokens) {
        String state = normalize(place.getState());
        if (state.isEmpty()) {
            return false;
        }
        return tokens.stream().anyMatch(token -> state.equals(token) || (token.length() > 2 && state.contains(token)));
    }

    private boolean isWithinBudget(Places place, BigDecimal totalBudget, BigDecimal dailyBudget) {
        if (totalBudget == null) {
            return true;
        }

        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            if (place.getEstimatedCost() != null) {
                return place.getEstimatedCost().compareTo(BigDecimal.ZERO) == 0;
            }
            return place.getPrice() == Price.GRATUITO;
        }

        if (place.getEstimatedCost() != null) {
            return place.getEstimatedCost().compareTo(totalBudget) <= 0
                    && (dailyBudget == null || place.getEstimatedCost().compareTo(dailyBudget.multiply(BigDecimal.valueOf(1.5))) <= 0
                    || place.getEstimatedCost().compareTo(totalBudget) <= 0);
        }

        if (place.getPrice() == null) {
            return true;
        }

        BigDecimal reference = dailyBudget != null ? dailyBudget : totalBudget;
        return switch (place.getPrice()) {
            case GRATUITO -> true;
            case BARATO -> totalBudget.compareTo(BigDecimal.valueOf(20)) >= 0;
            case MODERADO -> totalBudget.compareTo(BigDecimal.valueOf(80)) >= 0
                    && reference.compareTo(BigDecimal.valueOf(40)) >= 0;
            case CARO -> totalBudget.compareTo(BigDecimal.valueOf(200)) >= 0
                    && reference.compareTo(BigDecimal.valueOf(120)) >= 0;
        };
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalize(text).split("[-,|/]+"))
                .map(String::trim)
                .filter(s -> s.length() >= 2)
                .toList();
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
