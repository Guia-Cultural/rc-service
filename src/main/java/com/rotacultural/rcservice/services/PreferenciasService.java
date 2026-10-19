package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Preferencias.PrefReq;
import com.rotacultural.rcservice.DTOs.Preferencias.PrefResp;
import com.rotacultural.rcservice.entities.Categorias;
import com.rotacultural.rcservice.entities.Preferencias;
import com.rotacultural.rcservice.entities.Viagens;
import com.rotacultural.rcservice.repositories.CategoriasRepository;
import com.rotacultural.rcservice.repositories.PreferenciasRepository;
import com.rotacultural.rcservice.repositories.ViagensRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PreferenciasService {
    private final PreferenciasRepository preferenciasRepository;
    private final CategoriasRepository categoriasRepository;
    private final ViagensRepository viagensRepository;

    public PreferenciasService(PreferenciasRepository preferenciasRepository, CategoriasRepository categoriasRepository, ViagensRepository viagensRepository) {
        this.preferenciasRepository = preferenciasRepository;
        this.categoriasRepository = categoriasRepository;
        this.viagensRepository = viagensRepository;
    }

    public PrefResp create(PrefReq prefReq) {
        Preferencias preferencias = new Preferencias();
        Viagens viagens = viagensRepository.findById(prefReq.getTripId()).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        Categorias categorias = categoriasRepository.findById(prefReq.getCatId()).orElseThrow(()-> new EntityNotFoundException("Categoria não encontrada"));
        preferencias.setCategorias(categorias);
        preferencias.setViagens(viagens);
        PrefResp prefResp = new PrefResp();
        prefResp.setId(preferencias.getIdPref());
        prefResp.setCategorias(categorias);
        prefResp.setViagens(viagens);
        return prefResp;
    }

    public List<PrefResp> getAll() {
        List<Preferencias> preferencias = preferenciasRepository.findAll();
        List<PrefResp> prefResp = new ArrayList<>();
        for (Preferencias preferencias1 : preferencias) {
            PrefResp prefResp1 = new PrefResp();
            prefResp1.setId(preferencias1.getIdPref());
            prefResp1.setCategorias(preferencias1.getCategorias());
            prefResp1.setViagens(preferencias1.getViagens());
            prefResp.add(prefResp1);
        }
        return prefResp;
    }

    public PrefResp getById(Long id) {
        Preferencias preferencias = preferenciasRepository.getById(id);
        PrefResp prefResp = new PrefResp();
        prefResp.setId(preferencias.getIdPref());
        prefResp.setCategorias(preferencias.getCategorias());
        prefResp.setViagens(preferencias.getViagens());
        return prefResp;
    }
}
