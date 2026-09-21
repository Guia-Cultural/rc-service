package com.rotacultural.rcservice.DTOs.Viagens;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripReq {
    private long userId;

    @NotBlank
    @Size(max = 150)
    private String destino;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    @NotNull
    @PositiveOrZero
    private BigDecimal orcamento;
}
