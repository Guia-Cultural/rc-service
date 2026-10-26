
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Categories.CatReq;
import com.rotacultural.rcservice.DTOs.Categories.CatResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoriesRepository categoriesRepository;

    public CategoryService(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    public CatResp create(CatReq req) {
        Categories category = new Categories();
        category.setName(req.getName());

        return toResponse(categoriesRepository.save(category));
    }

    public List<CatResp> getAll() {
        return categoriesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CatResp getById(long id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        return toResponse(category);
    }

    public CatResp update(long id, CatReq req) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        category.setName(req.getName());

        return toResponse(categoriesRepository.save(category));
    }

    public String delete(long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }

        categoriesRepository.deleteById(id);
        return "Categoria removida com sucesso";
    }

    private CatResp toResponse(Categories category) {
        CatResp response = new CatResp();
        response.setId(category.getIdCat());
        response.setName(category.getName());
        return response;
    }
}
