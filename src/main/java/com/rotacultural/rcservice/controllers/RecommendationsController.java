package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.recommendations.RecommendationReq;
import com.rotacultural.rcservice.dtos.recommendations.RecommendationResp;
import com.rotacultural.rcservice.services.RecommendationsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    public RecommendationsController(RecommendationsService recommendationsService) {
        this.recommendationsService = recommendationsService;
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<RecommendationResp> recommendByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(recommendationsService.recommendForTrip(tripId));
    }

    @PostMapping("/preview")
    public ResponseEntity<RecommendationResp> preview(@RequestBody @Valid RecommendationReq req) {
        return ResponseEntity.ok(recommendationsService.preview(req));
    }
}
