package com.rotacultural.rcservice.dtos.feedbacks;

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
    private Double assessment;

    @Size(max = 500)
    private String commentary;
}
