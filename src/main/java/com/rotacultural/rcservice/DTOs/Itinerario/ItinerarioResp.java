package com.rotacultural.rcservice.DTOs.Itinerario;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItinerarioResp {
    private long id;
    private long tripId;
    private LocalDate dia;
    private long locId;
    private String descricao;

}
