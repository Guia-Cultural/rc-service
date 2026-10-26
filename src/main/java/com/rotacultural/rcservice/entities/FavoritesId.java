
package com.rotacultural.rcservice.entities;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long user;
    private Long place;
}
