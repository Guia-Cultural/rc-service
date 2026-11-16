package com.rotacultural.rcservice.dtos.recommendations;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationReq {
    @Size(max = 150)
    private String destination;

    private List<Long> categoryIds;

    @PositiveOrZero
    private BigDecimal budget;

    @Min(1)
    @Max(30)
    private Integer days;
}
