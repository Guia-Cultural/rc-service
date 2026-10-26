package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.favorites.FavReq;
import com.rotacultural.rcservice.services.FavoritesService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid FavReq req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(favoritesService.create(req));
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
        return ResponseEntity.ok(favoritesService.getAll());
    }

    @GetMapping("/{userId}/{locId}")
    public ResponseEntity<?> readById(
            @PathVariable Long userId,
            @PathVariable Long locId
    ) {
        try {
            return ResponseEntity.ok(
                    favoritesService.getById(userId, locId)
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{locId}")
    public ResponseEntity<?> delete(
            @PathVariable Long userId,
            @PathVariable Long locId
    ) {
        try {
            favoritesService.delete(userId, locId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}