package com.rotacultural.rcservice.DTOs.Categorias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatReq {
    @NotBlank
    @Size(min = 1, max = 75)
    private String nome;
}
