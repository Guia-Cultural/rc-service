package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.favorites.FavReq;
import com.rotacultural.rcservice.services.FavoritesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid FavReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoritesService.create(req));
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok(favoritesService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<?> readMyFavorites() {
        return ResponseEntity.ok(favoritesService.getMyFavorites());
    }

    @GetMapping("/{userId}/{locId}")
    public ResponseEntity<?> readById(
            @PathVariable Long userId,
            @PathVariable Long locId
    ) {
        return ResponseEntity.ok(favoritesService.getById(userId, locId));
    }

    @DeleteMapping("/{userId}/{locId}")
    public ResponseEntity<?> delete(
            @PathVariable Long userId,
            @PathVariable Long locId
    ) {
        favoritesService.delete(userId, locId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/place/{locId}")
    public ResponseEntity<?> deleteMyFavorite(@PathVariable Long locId) {
        favoritesService.deleteByPlace(locId);
        return ResponseEntity.noContent().build();
    }
}
