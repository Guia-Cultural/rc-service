package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Categories.CatReq;
import com.rotacultural.rcservice.DTOs.Categories.CatResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategorieService {
    private final CategoriesRepository categoriesRepository;

    public CategorieService(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    public CatResp create(CatReq catReq) {
        Categories categories = new Categories();
        categories.setNome(catReq.getNome());
        categoriesRepository.save(categories);
        CatResp catResp = new CatResp();
        catResp.setNome(catReq.getNome());
        return catResp;
    }

    public List<CatResp> getAll() {
        List<Categories> categorias = categoriesRepository.findAll();
        List<CatResp> catResps = new ArrayList<>();
        for (Categories categoria : categorias) {
            CatResp catResp = new CatResp();
            catResp.setNome(categoria.getNome());
            catResps.add(catResp);
        }
        return catResps;
    }

    public CatResp getById(long id) {
        Categories categories = categoriesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        CatResp catResp = new CatResp();
        catResp.setNome(categories.getNome());

        return catResp;
    }

    public CatResp update(long id, CatReq catReq) {
        Categories categoria = categoriesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        categoria.setNome(catReq.getNome());
        categoriesRepository.save(categoria);
        CatResp catResp = new CatResp();
        catResp.setNome(catReq.getNome());
        return catResp;
    }

    public String delete(long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }

        categoriesRepository.deleteById(id);
        return "Categoria removida com sucesso";
    }
}
