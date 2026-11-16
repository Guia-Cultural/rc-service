package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.dtos.users.RoleUpdateReq;
import com.rotacultural.rcservice.dtos.users.UserReq;
import com.rotacultural.rcservice.dtos.users.UserUpdateReq;
import com.rotacultural.rcservice.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid UserReq userReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.create(userReq));
    }

    @GetMapping
    public ResponseEntity<?> read() {
        return ResponseEntity.ok().body(usersService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> read(@PathVariable long id) {
        return ResponseEntity.ok().body(usersService.getById(id));
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<?> readByCep(@PathVariable String cep) {
        return ResponseEntity.ok(usersService.getByCep(cep));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable long id,
            @RequestBody @Valid UserUpdateReq userReq
    ) {
        return ResponseEntity.ok(usersService.update(id, userReq));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateRolePatch(
            @PathVariable long id,
            @RequestBody @Valid RoleUpdateReq roleReq
    ) {
        return ResponseEntity.ok(usersService.updateRole(id, roleReq.getRole()));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRolePut(
            @PathVariable long id,
            @RequestBody @Valid RoleUpdateReq roleReq
    ) {
        return ResponseEntity.ok(usersService.updateRole(id, roleReq.getRole()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        usersService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trip")
    public ResponseEntity<?> readByViagens() {
        return ResponseEntity.ok().body(usersService.getAllTrip());
    }
}
