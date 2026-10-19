package com.rotacultural.rcservice.entities;

import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesId implements Serializable {

    private Long usuario;
    private Long locais;


}