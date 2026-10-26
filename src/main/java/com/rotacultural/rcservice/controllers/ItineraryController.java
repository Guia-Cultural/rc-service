package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.itineraries.ItineraryReq;
import com.rotacultural.rcservice.services.ItineraryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ItineraryReq req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(itineraryService.create(req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok(itineraryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(itineraryService.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody @Valid ItineraryReq req
    ) {
        try {
            return ResponseEntity.ok(itineraryService.update(id, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            itineraryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}