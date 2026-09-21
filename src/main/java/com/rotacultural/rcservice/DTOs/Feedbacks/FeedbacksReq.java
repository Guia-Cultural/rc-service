package com.rotacultural.rcservice.DTOs.Feedbacks;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbacksReq {
    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long locId;

    @NotNull
    @Min(0)
    @Max(5)
    private Double avaliacao;

    @Size(max = 500)
    private String comentario;
}
