package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trips {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTrip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    private Users usuario;

    @NotBlank
    @Size(max = 150)
    private String destino;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Column(precision = 10, scale = 2)
    private BigDecimal orcamento;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

}
