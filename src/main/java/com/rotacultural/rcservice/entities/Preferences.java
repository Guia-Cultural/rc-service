package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "preferences",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"idTrip", "idCat"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Preferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPref;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTrip", nullable = false)
    private Trips trips;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idCat", nullable = false)
    private Categories categories;
}
