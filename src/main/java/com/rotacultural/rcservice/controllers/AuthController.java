package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.auth.LoginReq;
import com.rotacultural.rcservice.dtos.auth.LoginResp;
import com.rotacultural.rcservice.dtos.users.UserResp;
import com.rotacultural.rcservice.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResp> login(@RequestBody @Valid LoginReq req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResp> me() {
        return ResponseEntity.ok(authService.me());
    }
}
