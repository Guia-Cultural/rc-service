package com.rotacultural.rcservice.dtos.users;

import com.rotacultural.rcservice.entities.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateReq {
    @NotNull(message = "Role é obrigatória")
    private Role role;
}
