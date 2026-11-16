package com.rotacultural.rcservice.dtos.users;

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
    private String name;
    private String email;
    private String cep;
    private Role role;
    private Boolean nativeRequested;
    private String bio;
    private LocalDateTime dateCreate;
}
