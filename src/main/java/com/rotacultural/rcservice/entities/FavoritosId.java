package com.rotacultural.rcservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritosId implements Serializable {

    private Long usuario;
    private Long locais;
}