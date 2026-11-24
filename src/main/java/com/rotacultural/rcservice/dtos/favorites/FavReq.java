package com.rotacultural.rcservice.dtos.favorites;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavReq {
    private Long userId;

    @NotNull(message = "Local é obrigatório")
    @Positive
    private Long locId;
}
