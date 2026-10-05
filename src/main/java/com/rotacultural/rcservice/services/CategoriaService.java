package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Categorias.CatReq;
import com.rotacultural.rcservice.DTOs.Categorias.CatResp;
import com.rotacultural.rcservice.entities.Categorias;
import com.rotacultural.rcservice.repositories.CategoriasRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriasRepository categoriasRepository;

    public CategoriaService(CategoriasRepository categoriasRepository) {
        this.categoriasRepository = categoriasRepository;
    }

    public CatResp create(CatReq catReq) {
        Categorias categorias = new Categorias();
        categorias.setNome(catReq.getNome());
        categoriasRepository.save(categorias);
        CatResp catResp = new CatResp();
        catResp.setNome(catReq.getNome());
        return catResp;
    }

    public List<CatResp> getAll() {
        List<Categorias> categorias = categoriasRepository.findAll();
        List<CatResp> catResps = new ArrayList<>();
        for (Categorias categoria : categorias) {
            CatResp catResp = new CatResp();
            catResp.setNome(categoria.getNome());
            catResps.add(catResp);
        }
        return catResps;
    }

    public CatResp getById(long id) {
        Categorias categorias = categoriasRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        CatResp catResp = new CatResp();
        catResp.setNome(categorias.getNome());

        return catResp;
    }

    public CatResp update(long id, CatReq catReq) {
        Categorias categoria = categoriasRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        categoria.setNome(catReq.getNome());
        categoriasRepository.save(categoria);
        CatResp catResp = new CatResp();
        catResp.setNome(catReq.getNome());
        return catResp;
    }

    public String delete(long id) {
        if (!categoriasRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }

        categoriasRepository.deleteById(id);
        return "Categoria removida com sucesso";
    }
}
