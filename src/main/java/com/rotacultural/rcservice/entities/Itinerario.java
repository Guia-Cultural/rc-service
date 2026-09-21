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
public class Itinerario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItinerario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTrip", nullable = false)
    private Viagens viagem;

    @NotNull
    private LocalDate dia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idLoc", nullable = false)
    private Locais local;

    @Size(max = 500)
    private String descricao;
}
