
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Preferences.PrefReq;
import com.rotacultural.rcservice.DTOs.Preferences.PrefResp;
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

    public PreferencesService(
            PreferencesRepository preferencesRepository,
            CategoriesRepository categoriesRepository,
            TripsRepository tripsRepository
    ) {
        this.preferencesRepository = preferencesRepository;
        this.categoriesRepository = categoriesRepository;
        this.tripsRepository = tripsRepository;
    }

    public PrefResp create(PrefReq req) {
        Trips trip = tripsRepository.findById(req.getTripId())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

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

        Preferences preferenciaSalva = preferencesRepository.save(preference);

        return toResponse(preferenciaSalva);
    }

    public List<PrefResp> getAll() {
        return preferencesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PrefResp getById(Long id) {
        Preferences preference = preferencesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Preferência não encontrada"));

        return toResponse(preference);
    }


    private PrefResp toResponse(Preferences preference) {
        PrefResp response = new PrefResp();

        response.setId(preference.getIdPref());
        response.setTripId(preference.getTrips().getIdTrip());
        response.setCatId(preference.getCategories().getIdCat());

        return response;
    }

}
