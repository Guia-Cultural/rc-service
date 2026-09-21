package com.rotacultural.rcservice.DTOs.Feedbacks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbacksReq {
    private long userId;
    private long locId;

    @NotNull
    @Min(0)
    @Max(5)
    private Double avaliacao;

    @Size(max = 500)
    private String comentario;
}
