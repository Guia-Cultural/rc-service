package com.rotacultural.rcservice.DTOs.Categorias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatResp {
    private Long id;
    private String nome;
}
