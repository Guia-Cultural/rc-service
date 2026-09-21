package com.rotacultural.rcservice.DTOs.Usuarios;

import com.rotacultural.rcservice.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResp {
    private Long id;
    private String nome;
    private String email;
    private String cep;
    private Role role;
    private String bio;
    private LocalDateTime dataCriacao;
}
