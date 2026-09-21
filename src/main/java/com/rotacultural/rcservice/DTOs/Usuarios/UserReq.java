package com.rotacultural.rcservice.DTOs.Usuarios;

import com.rotacultural.rcservice.entities.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReq {
    @Size(max = 150)
    @NotBlank
    private String nome;

    @Size(max = 150)
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    @Size(max = 11)
    private String cep;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Size(max = 500)
    private String bio;
}
