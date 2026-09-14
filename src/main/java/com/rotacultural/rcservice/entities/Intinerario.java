package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Intinerario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIntinerario;

    @ManyToOne
    @JoinColumn(name = "idTrip")
    private Viagens viagens;

    @NotNull
    private Date dia;

    @ManyToOne
    @JoinColumn(name = "idLoc")
    private Locais locais;

    @Size(max = 500)
    private String descricao;
}
