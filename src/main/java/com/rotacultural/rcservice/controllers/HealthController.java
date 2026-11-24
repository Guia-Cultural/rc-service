package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.services.storage.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final StorageService storageService;

    public HealthController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "rc-service",
                "storageProvider", storageService.providerName()
        ));
    }
}
