package com.rotacultural.rcservice.DTOs.Preferencias;

import com.rotacultural.rcservice.entities.Categorias;
import com.rotacultural.rcservice.entities.Viagens;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefResp {
    private Long id;
    private Viagens viagens;
    private Categorias categorias;
}
