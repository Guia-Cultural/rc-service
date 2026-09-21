package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "preferencias",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"idTrip", "idCat"})
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Preferencias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPref;

    @ManyToOne
    @JoinColumn(name = "idTrip")
    private Viagens viagens;

    @ManyToOne
    @JoinColumn(name = "idCat")
    private Categorias categorias;
}
