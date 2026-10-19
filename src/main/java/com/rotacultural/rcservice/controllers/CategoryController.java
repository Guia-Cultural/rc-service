package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Categories.CatReq;
import com.rotacultural.rcservice.services.CategorieService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categoria")
public class CategoryController {
    private final CategorieService categorieService;

    public CategoryController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CatReq catReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.create(catReq));
    }

    @GetMapping
    public ResponseEntity<?> readAll(){
        return ResponseEntity.ok().body(categorieService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id){
        try{
            return ResponseEntity.ok().body(categorieService.getById(id));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid CatReq catReq) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categorieService.update(id, catReq));
        }catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try {
            categorieService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }


}
