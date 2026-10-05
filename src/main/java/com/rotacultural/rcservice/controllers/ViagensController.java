package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Viagens.TripReq;
import com.rotacultural.rcservice.services.ViagensService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/viagens")
public class ViagensController {
    private final ViagensService viagensService;

    public ViagensController(ViagensService viagensService) {
        this.viagensService = viagensService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid TripReq tripReq){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(viagensService.create(tripReq));
        }catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> readAll(){
        return ResponseEntity.ok().body(viagensService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable long id){
        try {
            return ResponseEntity.ok().body(viagensService.getById(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid TripReq tripReq){
        try {
            return ResponseEntity.ok().body(viagensService.update(id, tripReq));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try {
            viagensService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
