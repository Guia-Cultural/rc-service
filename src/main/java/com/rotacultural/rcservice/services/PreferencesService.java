package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.preferences.PrefReq;
import com.rotacultural.rcservice.dtos.preferences.PrefResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Preferences;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.PreferencesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PreferencesService {

    private final PreferencesRepository preferencesRepository;
    private final CategoriesRepository categoriesRepository;
    private final TripsRepository tripsRepository;
    private final AuthService authService;

    public PreferencesService(
            PreferencesRepository preferencesRepository,
            CategoriesRepository categoriesRepository,
            TripsRepository tripsRepository,
            AuthService authService
    ) {
        this.preferencesRepository = preferencesRepository;
        this.categoriesRepository = categoriesRepository;
        this.tripsRepository = tripsRepository;
        this.authService = authService;
    }

    public PrefResp create(PrefReq req) {
        Trips trip = tripsRepository.findById(req.getTripId())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        Categories categoria = categoriesRepository.findById(req.getCatId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        if (preferencesRepository.existsByTrips_IdTripAndCategories_IdCat(
                req.getTripId(),
                req.getCatId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta categoria já está associada à viagem"
            );
        }

        Preferences preference = new Preferences();
        preference.setCategories(categoria);
        preference.setTrips(trip);

        return toResponse(preferencesRepository.save(preference));
    }

    public List<PrefResp> getAll() {
        return preferencesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PrefResp> getByTrip(Long tripId) {
        Trips trip = tripsRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        return preferencesRepository.findByTrips_IdTrip(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PrefResp getById(Long id) {
        Preferences preference = preferencesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Preferência não encontrada"));

        authService.requireOwnerOrAdmin(preference.getTrips().getUser().getIdUser());

        return toResponse(preference);
    }

    public PrefResp update(Long id, PrefReq req) {
        Preferences preference = preferencesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Preferência não encontrada"));

        authService.requireOwnerOrAdmin(preference.getTrips().getUser().getIdUser());

        Trips trip = tripsRepository.findById(req.getTripId())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        authService.requireOwnerOrAdmin(trip.getUser().getIdUser());

        Categories category = categoriesRepository.findById(req.getCatId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        boolean changed = !preference.getTrips().getIdTrip().equals(req.getTripId())
                || !preference.getCategories().getIdCat().equals(req.getCatId());

        if (changed && preferencesRepository.existsByTrips_IdTripAndCategories_IdCat(
                req.getTripId(), req.getCatId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta categoria já está associada à viagem"
            );
        }

        preference.setTrips(trip);
        preference.setCategories(category);

        return toResponse(preferencesRepository.save(preference));
    }

    public void delete(Long id) {
        Preferences preference = preferencesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Preferência não encontrada"));

        authService.requireOwnerOrAdmin(preference.getTrips().getUser().getIdUser());

        preferencesRepository.delete(preference);
    }

    private PrefResp toResponse(Preferences preference) {
        PrefResp response = new PrefResp();
        response.setId(preference.getIdPref());
        response.setTripId(preference.getTrips().getIdTrip());
        response.setCatId(preference.getCategories().getIdCat());
        response.setCategoryName(preference.getCategories().getName());
        return response;
    }
}
