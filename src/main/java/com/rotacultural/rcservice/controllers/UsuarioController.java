package com.rotacultural.rcservice.controllers;

import com.rotacultural.rcservice.DTOs.Usuarios.UserReq;
import com.rotacultural.rcservice.services.UsuariosService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuariosService usuariosService;

    public UsuarioController(UsuariosService usuariosService) {
        this.usuariosService = usuariosService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid UserReq userReq){
        return ResponseEntity.status(HttpStatus.CREATED).body(usuariosService.create(userReq));
    }

    @GetMapping
    public ResponseEntity<?> read(){
        return ResponseEntity.ok().body(usuariosService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> read(@PathVariable long id){
        try{
            return ResponseEntity.ok().body(usuariosService.getById(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @GetMapping("/{cep}")
    public ResponseEntity<?> readByCep(@PathVariable String cep){
        try {
            return ResponseEntity.ok().body(usuariosService.getByCep(cep));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody @Valid UserReq userReq){
        try {
            return ResponseEntity.ok().body(usuariosService.update(id, userReq));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @DeleteMapping
    public ResponseEntity<?> delete(@PathVariable long id){
        try{
            usuariosService.delete(id);
            return ResponseEntity.noContent().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @GetMapping("/viagens")
    public ResponseEntity<?> readByViagens(){
        return ResponseEntity.ok().body(usuariosService.getAllTrip());
    }
}
