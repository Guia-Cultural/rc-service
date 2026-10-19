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
public class CategoryService {
    private final CategoriesRepository categoriesRepository;

    public CategoryService(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    public CatResp create(CatReq catReq) {
        Categories categories = new Categories();
        categories.setName(catReq.getName());
        categoriesRepository.save(categories);
        CatResp catResp = new CatResp();
        catResp.setName(catReq.getName());
        return catResp;
    }

    public List<CatResp> getAll() {
        List<Categories> categories = categoriesRepository.findAll();
        List<CatResp> catResps = new ArrayList<>();
        for (Categories category : categories) {
            CatResp catResp = new CatResp();
            catResp.setName(category.getName());
            catResps.add(catResp);
        }
        return catResps;
    }

    public CatResp getById(long id) {
        Categories categories = categoriesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        CatResp catResp = new CatResp();
        catResp.setName(categories.getName());

        return catResp;
    }

    public CatResp update(long id, CatReq catReq) {
        Categories category = categoriesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        category.setName(catReq.getName());
        categoriesRepository.save(category);
        CatResp catResp = new CatResp();
        catResp.setName(catReq.getName());
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
