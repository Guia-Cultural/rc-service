package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Trips.TripReq;
import com.rotacultural.rcservice.services.TripsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/viagens")
public class TripController {
    private final TripsService tripsService;

    public TripController(TripsService tripsService) {
        this.tripsService = tripsService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid TripReq tripReq){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(tripsService.create(tripReq));
        }catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> readAll(){
        return ResponseEntity.ok().body(tripsService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable long id){
        try {
            return ResponseEntity.ok().body(tripsService.getById(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid TripReq tripReq){
        try {
            return ResponseEntity.ok().body(tripsService.update(id, tripReq));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try {
            tripsService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
