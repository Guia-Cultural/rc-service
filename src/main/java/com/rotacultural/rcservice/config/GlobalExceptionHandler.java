package com.rotacultural.rcservice.config;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : "Erro na requisição";
        return ResponseEntity.status(ex.getStatusCode())
                .body(buildBody(status, message, null));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND.value(), message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        String summary = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        if (summary.isBlank()) {
            summary = "Dados inválidos na requisição";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), summary, fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), "Corpo da requisição inválido ou malformado", null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), "Arquivo excede o limite máximo de 5MB", null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT.value(),
                        "Conflito de integridade ao processar os dados solicitados", null));
    }

    private Map<String, Object> buildBody(int status, String message, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now().toString());
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        return body;
    }
}
