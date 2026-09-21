package com.rotacultural.rcservice.DTOs.Preferencias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefReq {
    @NotNull
    @Positive
    private Long tripId;

    @NotNull
    @Positive
    private Long catId;
}
