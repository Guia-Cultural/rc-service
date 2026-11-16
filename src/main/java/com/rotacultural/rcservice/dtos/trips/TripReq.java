package com.rotacultural.rcservice.dtos.trips;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripReq {
    private Long userId;

    @NotBlank(message = "Destino é obrigatório")
    @Size(max = 150)
    private String destination;

    @NotNull(message = "Data inicial é obrigatória")
    private LocalDate startDate;

    @NotNull(message = "Data final é obrigatória")
    private LocalDate endDate;

    @NotNull(message = "Orçamento é obrigatório")
    @PositiveOrZero(message = "Orçamento não pode ser negativo")
    private BigDecimal budget;

    private List<Long> categoryIds;
}
