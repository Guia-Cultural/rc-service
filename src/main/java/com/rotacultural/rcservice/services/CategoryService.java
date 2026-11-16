package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.categories.CatReq;
import com.rotacultural.rcservice.dtos.categories.CatResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.PreferencesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoriesRepository categoriesRepository;
    private final PlacesRepository placesRepository;
    private final PreferencesRepository preferencesRepository;

    public CategoryService(
            CategoriesRepository categoriesRepository,
            PlacesRepository placesRepository,
            PreferencesRepository preferencesRepository
    ) {
        this.categoriesRepository = categoriesRepository;
        this.placesRepository = placesRepository;
        this.preferencesRepository = preferencesRepository;
    }

    public CatResp create(CatReq req) {
        Categories category = new Categories();
        category.setName(req.getName().trim());

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

        category.setName(req.getName().trim());

        return toResponse(categoriesRepository.save(category));
    }

    @Transactional
    public String delete(long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }

        List<Places> linkedPlaces = placesRepository.findByCategory_IdCat(id);
        for (Places place : linkedPlaces) {
            place.setCategory(null);
            placesRepository.save(place);
        }
        preferencesRepository.deleteByCategories_IdCat(id);
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
