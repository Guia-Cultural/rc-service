package com.rotacultural.rcservice.DTOs.Viagens;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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
