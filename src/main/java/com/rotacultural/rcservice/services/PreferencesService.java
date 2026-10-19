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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PreferencesService {
    private final PreferencesRepository preferencesRepository;
    private final CategoriesRepository categoriesRepository;
    private final TripsRepository tripsRepository;

    public PreferencesService(PreferencesRepository preferencesRepository, CategoriesRepository categoriesRepository, TripsRepository tripsRepository) {
        this.preferencesRepository = preferencesRepository;
        this.categoriesRepository = categoriesRepository;
        this.tripsRepository = tripsRepository;
    }

    public PrefResp create(PrefReq prefReq) {
        Preferences preferences = new Preferences();
        Trips trips = tripsRepository.findById(prefReq.getTripId()).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        Categories categories = categoriesRepository.findById(prefReq.getCatId()).orElseThrow(()-> new EntityNotFoundException("Categoria não encontrada"));
        preferences.setCategories(categories);
        preferences.setTrips(trips);
        PrefResp prefResp = new PrefResp();
        prefResp.setId(preferences.getIdPref());
        prefResp.setCategories(categories);
        prefResp.setTrips(trips);
        return prefResp;
    }

    public List<PrefResp> getAll() {
        List<Preferences> preferencias = preferencesRepository.findAll();
        List<PrefResp> prefResp = new ArrayList<>();
        for (Preferences preferences1 : preferencias) {
            PrefResp prefResp1 = new PrefResp();
            prefResp1.setId(preferences1.getIdPref());
            prefResp1.setCategories(preferences1.getCategories());
            prefResp1.setTrips(preferences1.getTrips());
            prefResp.add(prefResp1);
        }
        return prefResp;
    }

    public PrefResp getById(Long id) {
        Preferences preferences = preferencesRepository.getById(id);
        PrefResp prefResp = new PrefResp();
        prefResp.setId(preferences.getIdPref());
        prefResp.setCategories(preferences.getCategories());
        prefResp.setTrips(preferences.getTrips());
        return prefResp;
    }
}
