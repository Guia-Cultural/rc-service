package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.feedbacks.FeedbacksReq;
import com.rotacultural.rcservice.services.FeedbacksService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedbacks")
public class FeedbacksController {

    private final FeedbacksService feedbacksService;

    public FeedbacksController(FeedbacksService feedbacksService) {
        this.feedbacksService = feedbacksService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid FeedbacksReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbacksService.create(req));
    }

    @GetMapping
    public ResponseEntity<?> readAll() {
        return ResponseEntity.ok(feedbacksService.getAll());
    }

    @GetMapping("/place/{locId}")
    public ResponseEntity<?> readByPlace(@PathVariable Long locId) {
        return ResponseEntity.ok(feedbacksService.getByPlace(locId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbacksService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody @Valid FeedbacksReq req
    ) {
        return ResponseEntity.ok(feedbacksService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        feedbacksService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
