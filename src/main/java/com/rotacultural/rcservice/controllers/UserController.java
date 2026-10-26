package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Users.UserReq;
import com.rotacultural.rcservice.DTOs.Users.UserUpdateReq;
import com.rotacultural.rcservice.services.UsersService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid UserReq userReq){
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.create(userReq));
    }

    @GetMapping
    public ResponseEntity<?> read(){
        return ResponseEntity.ok().body(usersService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> read(@PathVariable long id){
        try{
            return ResponseEntity.ok().body(usersService.getById(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<?> readByCep(@PathVariable String cep) {
        try {
            return ResponseEntity.ok(usersService.getByCep(cep));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable long id,
            @RequestBody @Valid UserUpdateReq userReq
    ) {
        try {
            return ResponseEntity.ok(usersService.update(id, userReq));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        try{
            usersService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @GetMapping("/viagens")
    public ResponseEntity<?> readByViagens(){
        return ResponseEntity.ok().body(usersService.getAllTrip());
    }
}
