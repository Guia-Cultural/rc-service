package com.rotacultural.rcservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritosId{

    private Long usuario;
    private Long locais;


}