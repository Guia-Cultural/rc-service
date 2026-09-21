package com.rotacultural.rcservice.DTOs.Viagens;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripReq {
    private long userId;

    @NotBlank
    @Size(min = 15, max = 150)
    private String destino;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    @NotNull
    private BigDecimal orcamento;
}
