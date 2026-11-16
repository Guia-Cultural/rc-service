package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.itineraries.GenerateItineraryReq;
import com.rotacultural.rcservice.dtos.itineraries.ItineraryReq;
import com.rotacultural.rcservice.services.ItineraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ItineraryReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itineraryService.create(req));
    }

    @PostMapping("/generate/{tripId}")
    public ResponseEntity<?> generateForTrip(
            @PathVariable Long tripId,
            @RequestBody(required = false) GenerateItineraryReq req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itineraryService.generateForTrip(tripId, req));
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok(itineraryService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<?> readMyItineraries() {
        return ResponseEntity.ok(itineraryService.getMyItineraries());
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> readByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(itineraryService.getByTrip(tripId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id) {
        return ResponseEntity.ok(itineraryService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody @Valid ItineraryReq req
    ) {
        return ResponseEntity.ok(itineraryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        itineraryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
