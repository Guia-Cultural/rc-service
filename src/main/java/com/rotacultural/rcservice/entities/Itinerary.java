package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItinerario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTrip", nullable = false)
    private Trips trip;

    @NotNull
    private LocalDate day;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idLoc", nullable = false)
    private Places locale;

    @Size(max = 500)
    private String description;
}
