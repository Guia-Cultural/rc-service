package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Categorias.CatReq;
import com.rotacultural.rcservice.services.CategoriaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CatReq catReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.create(catReq));
    }

    @GetMapping
    public ResponseEntity<?> readAll(){
        return ResponseEntity.ok().body(categoriaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id){
        try{
            return ResponseEntity.ok().body(categoriaService.getById(id));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid CatReq catReq) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoriaService.update(id, catReq));
        }catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoriaService.delete(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }

    }


}
