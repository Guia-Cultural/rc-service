
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

    public CatResp create(CatReq catReq) {
        Categories categoria = new Categories();
        categoria.setName(catReq.getName());

        return toResponse(categoriesRepository.save(categoria));
    }

    public List<CatResp> getAll() {
        return categoriesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CatResp getById(long id) {
        Categories categoria = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        return toResponse(categoria);
    }

    public CatResp update(long id, CatReq catReq) {
        Categories categoria = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        categoria.setName(catReq.getName());

        return toResponse(categoriesRepository.save(categoria));
    }

    public String delete(long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }

        categoriesRepository.deleteById(id);
        return "Categoria removida com sucesso";
    }

    private CatResp toResponse(Categories categoria) {
        CatResp resposta = new CatResp();
        resposta.setId(categoria.getIdCat());
        resposta.setName(categoria.getName());
        return resposta;
    }
}
