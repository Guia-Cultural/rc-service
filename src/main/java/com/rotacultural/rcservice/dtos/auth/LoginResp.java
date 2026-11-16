package com.rotacultural.rcservice.dtos.auth;

import com.rotacultural.rcservice.dtos.users.UserResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResp {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserResp user;
}
