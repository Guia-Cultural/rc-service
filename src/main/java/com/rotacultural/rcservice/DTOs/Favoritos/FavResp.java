package com.rotacultural.rcservice.DTOs.Favoritos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavResp {
    private Long userId;
    private Long locId;
    private LocalDateTime dataCadastro;
}
