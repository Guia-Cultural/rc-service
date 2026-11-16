package com.rotacultural.rcservice.dtos.recommendations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResp {
    private String destination;
    private Integer days;
    private BigDecimal budget;
    private boolean sufficientData;
    private String message;
    private List<RecommendedPlaceResp> places;
}
