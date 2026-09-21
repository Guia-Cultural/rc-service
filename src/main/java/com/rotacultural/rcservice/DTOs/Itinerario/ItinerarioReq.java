package com.rotacultural.rcservice.DTOs.Itinerario;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItinerarioReq {
    private long tripId;
    private Date dia;
    private long locId;
    @Size(max = 500)
    private String descricao;
}
