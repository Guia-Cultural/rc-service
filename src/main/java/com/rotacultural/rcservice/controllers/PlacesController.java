package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.places.LocReq;
import com.rotacultural.rcservice.dtos.places.PlaceStatusReq;
import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.services.PlacesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/places")
public class PlacesController {

    private final PlacesService placesService;

    public PlacesController(PlacesService placesService) {
        this.placesService = placesService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid LocReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placesService.create(req));
    }

    @GetMapping
    public ResponseEntity<?> readAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Long catId,
            @RequestParam(required = false) Price price
    ) {
        return ResponseEntity.ok(placesService.getPublicPlaces(q, city, state, catId, price));
    }

    @GetMapping("/paged")
    public ResponseEntity<?> readPaged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Long catId,
            @RequestParam(required = false) Price price,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(placesService.getPublicPlacesPaged(q, city, state, catId, price, page, size));
    }

    @GetMapping("/my")
    public ResponseEntity<?> readMyPlaces() {
        return ResponseEntity.ok(placesService.getMyPlaces());
    }

    @GetMapping("/admin")
    public ResponseEntity<?> readAdminPlaces(
            @RequestParam(required = false) Status status
    ) {
        return ResponseEntity.ok(placesService.getAdminPlaces(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id) {
        return ResponseEntity.ok(placesService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody @Valid LocReq req
    ) {
        return ResponseEntity.ok(placesService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatusPatch(
            @PathVariable Long id,
            @RequestBody @Valid PlaceStatusReq req
    ) {
        return ResponseEntity.ok(placesService.updateStatus(id, req));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatusPut(
            @PathVariable Long id,
            @RequestBody @Valid PlaceStatusReq req
    ) {
        return ResponseEntity.ok(placesService.updateStatus(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        placesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "false") boolean cover
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placesService.uploadImages(id, files, cover));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<?> listImages(@PathVariable Long id) {
        return ResponseEntity.ok(placesService.listImages(id));
    }

    @DeleteMapping("/{placeId}/images/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long placeId,
            @PathVariable Long imageId
    ) {
        placesService.deleteImage(placeId, imageId);
        return ResponseEntity.noContent().build();
    }
}
