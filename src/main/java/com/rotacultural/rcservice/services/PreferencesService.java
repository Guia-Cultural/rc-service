
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

    public PrefResp create(PrefReq prefReq) {
        Trips viagem = tripsRepository.findById(prefReq.getTripId())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        Categories categoria = categoriesRepository.findById(prefReq.getCatId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Preferences preferencia = new Preferences();
        preferencia.setCategories(categoria);
        preferencia.setTrips(viagem);

        Preferences preferenciaSalva = preferencesRepository.save(preferencia);

        return toResponse(preferenciaSalva);
    }

    public List<PrefResp> getAll() {
        return preferencesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PrefResp getById(Long id) {
        Preferences preferencia = preferencesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Preferência não encontrada"));

        return toResponse(preferencia);
    }

    private PrefResp toResponse(Preferences preferencia) {
        PrefResp resposta = new PrefResp();

        resposta.setId(preferencia.getIdPref());
        resposta.setCategories(preferencia.getCategories());
        resposta.setTrips(preferencia.getTrips());

        return resposta;
    }
}
