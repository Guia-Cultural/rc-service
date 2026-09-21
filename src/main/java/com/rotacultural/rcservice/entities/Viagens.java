package com.rotacultural.rcservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Viagens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTrip;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private Usuarios usuario;

    @NotBlank
    @Size(max = 150)
    private String destino;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = { "d/M/yy", "dd.MM.yyyy" })
    private LocalDate dataInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = { "d/M/yy", "dd.MM.yyyy" })
    private LocalDate dataFim;

    @Column(precision = 10, scale = 2)
    private BigDecimal orcamento;

    @CreationTimestamp
    @Column(name = "data_create", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

}
