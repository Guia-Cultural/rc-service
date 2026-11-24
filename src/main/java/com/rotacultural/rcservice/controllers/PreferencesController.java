package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.preferences.PrefReq;
import com.rotacultural.rcservice.services.PreferencesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preferences")
public class PreferencesController {

    private final PreferencesService preferencesService;

    public PreferencesController(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PrefReq prefReq) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(preferencesService.create(prefReq));
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok(preferencesService.getAll());
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> readByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(preferencesService.getByTrip(tripId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id) {
        return ResponseEntity.ok(preferencesService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody @Valid PrefReq prefReq
    ) {
        return ResponseEntity.ok(preferencesService.update(id, prefReq));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        preferencesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
