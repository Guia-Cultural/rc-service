package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.itineraries.GenerateItineraryReq;
import com.rotacultural.rcservice.dtos.itineraries.ItineraryReq;
import com.rotacultural.rcservice.dtos.itineraries.ItineraryResp;
import com.rotacultural.rcservice.entities.Itinerary;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.ItineraryRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ItineraryService {

    private static final String[] PERIOD_LABELS = {"Manhã", "Tarde", "Noite", "Fim de tarde", "Extra"};

    private final ItineraryRepository itineraryRepository;
    private final TripsRepository tripsRepository;
    private final PlacesRepository placesRepository;
    private final RecommendationsService recommendationsService;
    private final AuthService authService;

    public ItineraryService(
            ItineraryRepository itineraryRepository,
            TripsRepository tripsRepository,
            PlacesRepository placesRepository,
            RecommendationsService recommendationsService,
            AuthService authService
    ) {
        this.itineraryRepository = itineraryRepository;
        this.tripsRepository = tripsRepository;
        this.placesRepository = placesRepository;
        this.recommendationsService = recommendationsService;
        this.authService = authService;
    }

    public ItineraryResp create(ItineraryReq req) {
        Trips trip = getTrip(req.getTripId());
        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        Places place = getApprovedPlace(req.getLocId());
        validateDay(trip, req.getDay());

        Itinerary itinerary = new Itinerary();
        itinerary.setTrip(trip);
        itinerary.setLocale(place);
        itinerary.setDay(req.getDay());
        itinerary.setDescription(req.getDescription());

        return toResponse(itineraryRepository.save(itinerary));
    }

    @Transactional(readOnly = true)
    public List<ItineraryResp> getAll() {
        return itineraryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItineraryResp> getMyItineraries() {
        Users currentUser = authService.getCurrentUser();
        return itineraryRepository.findByTrip_User_IdUserOrderByDayAscIdItinerarioAsc(currentUser.getIdUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItineraryResp> getByTrip(Long tripId) {
        Trips trip = getTrip(tripId);
        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        return itineraryRepository.findByTrip_IdTripOrderByDayAscIdItinerarioAsc(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItineraryResp getById(Long id) {
        Itinerary itinerary = getItinerary(id);
        authService.requireOwnerOrAdmin(itinerary.getTrip().getUser().getIdUser());
        return toResponse(itinerary);
    }

    public ItineraryResp update(Long id, ItineraryReq req) {
        Itinerary itinerary = getItinerary(id);
        authService.requireOwnerOrAdmin(itinerary.getTrip().getUser().getIdUser());

        Trips trip = getTrip(req.getTripId());
        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        Places place = getApprovedPlace(req.getLocId());
        validateDay(trip, req.getDay());

        itinerary.setTrip(trip);
        itinerary.setLocale(place);
        itinerary.setDay(req.getDay());
        itinerary.setDescription(req.getDescription());

        return toResponse(itineraryRepository.save(itinerary));
    }

    public void delete(Long id) {
        Itinerary itinerary = getItinerary(id);
        authService.requireOwnerOrAdmin(itinerary.getTrip().getUser().getIdUser());
        itineraryRepository.delete(itinerary);
    }

    @Transactional
    public List<ItineraryResp> generateForTrip(Long tripId, GenerateItineraryReq req) {
        Trips trip = getTrip(tripId);
        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        if (trip.getStartDate() == null || trip.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A viagem precisa possuir datas inicial e final definidas para gerar o roteiro"
            );
        }

        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        if (totalDays < 1 || totalDays > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O período da viagem deve ter entre 1 e 30 dias"
            );
        }

        int maxPerDay = 3;
        if (req != null && req.getMaxPerDay() != null) {
            maxPerDay = Math.min(Math.max(req.getMaxPerDay(), 1), 5);
        }

        List<Places> selectedPlaces = resolvePlacesForGeneration(trip, req);
        if (selectedPlaces.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não existem locais culturais aprovados cadastrados para o destino e orçamento desta viagem"
            );
        }

        itineraryRepository.deleteByTrip_IdTrip(tripId);

        int maxSlots = (int) Math.min(selectedPlaces.size(), totalDays * maxPerDay);
        int itemsPerDay = Math.min(maxPerDay, (int) Math.ceil((double) maxSlots / totalDays));

        List<ItineraryResp> generated = new ArrayList<>();
        int index = 0;
        for (int dayOffset = 0; dayOffset < totalDays && index < maxSlots; dayOffset++) {
            LocalDate currentDay = trip.getStartDate().plusDays(dayOffset);
            for (int slot = 0; slot < itemsPerDay && index < maxSlots; slot++) {
                Places place = selectedPlaces.get(index++);
                String period = PERIOD_LABELS[Math.min(slot, PERIOD_LABELS.length - 1)];

                String description = "Período sugerido: " + period + " • Funcionamento: " + place.getBusinessHours();
                if (description.length() > 500) {
                    description = description.substring(0, 497) + "...";
                }

                Itinerary item = new Itinerary();
                item.setTrip(trip);
                item.setLocale(place);
                item.setDay(currentDay);
                item.setDescription(description);

                generated.add(toResponse(itineraryRepository.save(item)));
            }
        }

        return generated;
    }

    private List<Places> resolvePlacesForGeneration(Trips trip, GenerateItineraryReq req) {
        if (req != null && req.getPlaceIds() != null && !req.getPlaceIds().isEmpty()) {
            Set<Long> uniqueIds = new LinkedHashSet<>(req.getPlaceIds());
            List<Places> chosen = new ArrayList<>();
            for (Long id : uniqueIds) {
                if (id != null) {
                    chosen.add(getApprovedPlace(id));
                }
            }
            return chosen;
        }
        return recommendationsService.selectApprovedPlacesForTrip(trip);
    }

    private Itinerary getItinerary(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Itinerário não encontrado"));
    }

    private Trips getTrip(Long id) {
        return tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
    }

    private Places getApprovedPlace(Long id) {
        Places place = placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local não encontrado"));
        if (place.getStatus() != Status.APROVADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Apenas locais aprovados podem ser incluídos no roteiro"
            );
        }
        return place;
    }

    private void validateDay(Trips trip, LocalDate day) {
        if (trip.getStartDate() != null && day.isBefore(trip.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O dia do itinerário não pode ser anterior ao início da viagem"
            );
        }

        if (trip.getEndDate() != null && day.isAfter(trip.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O dia do itinerário não pode ser posterior ao fim da viagem"
            );
        }
    }

    private ItineraryResp toResponse(Itinerary itinerary) {
        ItineraryResp response = new ItineraryResp();
        Places place = itinerary.getLocale();

        response.setId(itinerary.getIdItinerario());
        response.setTripId(itinerary.getTrip().getIdTrip());
        response.setDay(itinerary.getDay());
        response.setLocId(place.getIdLoc());
        response.setDescription(itinerary.getDescription());
        response.setPlaceName(place.getName());
        response.setPlaceAddress(place.getAddress());
        response.setPlaceCity(place.getCity());
        response.setPlaceState(place.getState());
        response.setPlaceBusinessHours(place.getBusinessHours());
        response.setPrice(place.getPrice());
        response.setEstimatedCost(place.getEstimatedCost());
        response.setCoverImage(place.getCoverImage());
        response.setCategoryName(place.getCategory() != null ? place.getCategory().getName() : null);

        return response;
    }
}
