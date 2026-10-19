package com.rotacultural.rcservice.DTOs.Itineraries;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryReq {
    @NotNull
    @Positive
    private Long tripId;

    @NotNull
    private LocalDate dia;

    @NotNull
    @Positive
    private Long locId;

    @Size(max = 500)
    private String descricao;
}
