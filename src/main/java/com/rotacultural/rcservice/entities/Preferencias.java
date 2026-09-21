package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "preferencias",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"idTrip", "idCat"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Preferencias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPref;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTrip", nullable = false)
    private Viagens viagens;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idCat", nullable = false)
    private Categorias categorias;
}
