package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.categories.CatReq;
import com.rotacultural.rcservice.services.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CatReq catReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(catReq));
    }

    @GetMapping
    public ResponseEntity<?> readAll(){
        return ResponseEntity.ok().body(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id){
        try{
            return ResponseEntity.ok().body(categoryService.getById(id));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid CatReq catReq) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoryService.update(id, catReq));
        }catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }


}
