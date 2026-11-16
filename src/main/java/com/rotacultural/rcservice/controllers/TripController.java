package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.trips.TripReq;
import com.rotacultural.rcservice.services.TripsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trip")
public class TripController {
    private final TripsService tripsService;

    public TripController(TripsService tripsService) {
        this.tripsService = tripsService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid TripReq tripReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripsService.create(tripReq));
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok().body(tripsService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<?> readMyTrips() {
        return ResponseEntity.ok().body(tripsService.getMyTrips());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable long id) {
        return ResponseEntity.ok().body(tripsService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid TripReq tripReq) {
        return ResponseEntity.ok().body(tripsService.update(id, tripReq));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        tripsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
