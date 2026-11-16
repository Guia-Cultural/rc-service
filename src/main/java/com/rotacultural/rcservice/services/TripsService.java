package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.preferences.PrefResp;
import com.rotacultural.rcservice.dtos.trips.TripReq;
import com.rotacultural.rcservice.dtos.trips.TripResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Preferences;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.ItineraryRepository;
import com.rotacultural.rcservice.repositories.PreferencesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TripsService {

    private static final long MAX_TRIP_DAYS = 30;

    private final TripsRepository tripsRepository;
    private final PreferencesRepository preferencesRepository;
    private final CategoriesRepository categoriesRepository;
    private final ItineraryRepository itineraryRepository;
    private final AuthService authService;

    public TripsService(
            TripsRepository tripsRepository,
            PreferencesRepository preferencesRepository,
            CategoriesRepository categoriesRepository,
            ItineraryRepository itineraryRepository,
            AuthService authService
    ) {
        this.tripsRepository = tripsRepository;
        this.preferencesRepository = preferencesRepository;
        this.categoriesRepository = categoriesRepository;
        this.itineraryRepository = itineraryRepository;
        this.authService = authService;
    }

    @Transactional
    public TripResp create(TripReq req) {
        dateValidation(req);

        Users usuario = authService.getCurrentUser();
        if (req.getUserId() != null && !req.getUserId().equals(usuario.getIdUser()) && !authService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Não é permitido criar viagem para outro usuário"
            );
        }

        Trips trip = new Trips();
        trip.setUser(usuario);
        trip.setDestination(req.getDestination().trim());
        trip.setStartDate(req.getStartDate());
        trip.setEndDate(req.getEndDate());
        trip.setBudget(req.getBudget());

        Trips savedTrip = tripsRepository.save(trip);
        saveCategoriesIfProvided(savedTrip, req.getCategoryIds());

        return toResponse(savedTrip);
    }

    @Transactional(readOnly = true)
    public List<TripResp> getAll() {
        return tripsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripResp> getMyTrips() {
        Users currentUser = authService.getCurrentUser();
        return tripsRepository.findByUser_IdUserOrderByDateCreateDesc(currentUser.getIdUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResp getById(Long id) {
        Trips trip = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        return toResponse(trip);
    }

    @Transactional
    public TripResp update(long id, TripReq req) {
        dateValidation(req);

        Trips trip = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        trip.setDestination(req.getDestination().trim());
        trip.setStartDate(req.getStartDate());
        trip.setEndDate(req.getEndDate());
        trip.setBudget(req.getBudget());

        Trips savedTrip = tripsRepository.save(trip);

        if (req.getCategoryIds() != null) {
            preferencesRepository.deleteByTrips_IdTrip(savedTrip.getIdTrip());
            saveCategoriesIfProvided(savedTrip, req.getCategoryIds());
        }

        return toResponse(savedTrip);
    }

    @Transactional
    public void delete(long id) {
        Trips trip = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        preferencesRepository.deleteByTrips_IdTrip(id);
        itineraryRepository.deleteByTrip_IdTrip(id);
        tripsRepository.delete(trip);
    }

    private void saveCategoriesIfProvided(Trips trip, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(categoryIds);
        for (Long catId : uniqueIds) {
            if (catId == null) {
                continue;
            }
            Categories category = categoriesRepository.findById(catId)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
            if (!preferencesRepository.existsByTrips_IdTripAndCategories_IdCat(trip.getIdTrip(), catId)) {
                Preferences pref = new Preferences();
                pref.setTrips(trip);
                pref.setCategories(category);
                preferencesRepository.save(pref);
            }
        }
    }

    private void dateValidation(TripReq req) {
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "As datas inicial e final são obrigatórias"
            );
        }

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data final não pode ser anterior à data inicial"
            );
        }

        long totalDays = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        if (totalDays > MAX_TRIP_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A viagem pode ter no máximo " + MAX_TRIP_DAYS + " dias"
            );
        }
    }

    private TripResp toResponse(Trips trip) {
        TripResp response = new TripResp();
        response.setId(trip.getIdTrip());
        response.setUserId(trip.getUser().getIdUser());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setBudget(trip.getBudget());
        response.setDateCreate(trip.getDateCreate());

        List<PrefResp> prefs = preferencesRepository.findByTrips_IdTrip(trip.getIdTrip())
                .stream()
                .map(p -> new PrefResp(
                        p.getIdPref(),
                        trip.getIdTrip(),
                        p.getCategories().getIdCat(),
                        p.getCategories().getName()
                ))
                .toList();
        response.setPreferences(prefs);
        response.setItineraryCount((int) itineraryRepository.countByTrip_IdTrip(trip.getIdTrip()));

        return response;
    }
}
