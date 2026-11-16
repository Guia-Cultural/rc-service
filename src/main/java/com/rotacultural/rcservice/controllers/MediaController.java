package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.services.AuthService;
import com.rotacultural.rcservice.services.storage.ImageFileValidator;
import com.rotacultural.rcservice.services.storage.MediaAccessService;
import com.rotacultural.rcservice.services.storage.StorageService;
import com.rotacultural.rcservice.services.storage.StoredResource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

@RestController
public class MediaController {

    private final PlacesRepository placesRepository;
    private final StorageService storageService;
    private final AuthService authService;
    private final MediaAccessService mediaAccessService;

    public MediaController(
            PlacesRepository placesRepository,
            StorageService storageService,
            AuthService authService,
            MediaAccessService mediaAccessService
    ) {
        this.placesRepository = placesRepository;
        this.storageService = storageService;
        this.authService = authService;
        this.mediaAccessService = mediaAccessService;
    }

    @GetMapping("/uploads/places/{placeId}/{filename:.+}")
    public ResponseEntity<byte[]> getPlaceImage(
            @PathVariable Long placeId,
            @PathVariable String filename,
            @RequestParam(required = false) Long exp,
            @RequestParam(required = false) String sig
    ) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de imagem inválido");
        }

        Places place = placesRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));

        String canonicalUrl = ImageFileValidator.UPLOADS_PREFIX + "places/" + placeId + "/" + filename;

        if (place.getStatus() != Status.APROVADO) {
            boolean authorizedBySignature = mediaAccessService.isValidSignature(canonicalUrl, exp, sig);
            boolean authorizedByUser = false;
            if (!authorizedBySignature) {
                Optional<Users> currentUser = authService.getOptionalCurrentUser();
                authorizedByUser = currentUser.isPresent() && (
                        currentUser.get().getRole() == Role.ADMIN
                                || place.getCreateBy().getIdUser().equals(currentUser.get().getIdUser())
                );
            }
            if (!authorizedBySignature && !authorizedByUser) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Imagem disponível apenas após aprovação do local ou autorização"
                );
            }
        }

        StoredResource resource = storageService.load(canonicalUrl);
        CacheControl cacheControl = place.getStatus() == Status.APROVADO
                ? CacheControl.maxAge(Duration.ofHours(1)).cachePublic()
                : CacheControl.noStore();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.contentType()))
                .cacheControl(cacheControl)
                .body(resource.content());
    }
}
