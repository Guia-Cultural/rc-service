package com.rotacultural.rcservice.DTOs.Users;

import com.rotacultural.rcservice.entities.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 11)
    private String cep;
    
    @NotNull(message = "Role é obrigatória")
    private Role role;

    @Size(max = 500)
    private String bio;
}
