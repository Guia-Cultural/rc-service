package com.rotacultural.rcservice.dtos.feedbacks;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbacksReq {
    private Long userId;

    @NotNull(message = "Local é obrigatório")
    @Positive
    private Long locId;

    @NotNull(message = "Nota é obrigatória")
    @Min(0)
    @Max(5)
    private Double assessment;

    @Size(max = 500)
    private String commentary;
}
