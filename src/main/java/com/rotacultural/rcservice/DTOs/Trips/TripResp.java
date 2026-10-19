package com.rotacultural.rcservice.DTOs.Trips;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripResp {
    private Long id;
    private Long userId;
    private String destino;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal orcamento;
    private LocalDateTime dataCriacao;


}
