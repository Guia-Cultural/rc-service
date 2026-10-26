
package com.rotacultural.rcservice.DTOs.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateReq {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 11)
    private String cep;

    @Size(max = 500)
    private String bio;
}
