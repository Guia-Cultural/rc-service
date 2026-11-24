package com.rotacultural.rcservice.dtos.trips;

import com.rotacultural.rcservice.dtos.preferences.PrefResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripResp {
    private Long id;
    private Long userId;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private LocalDateTime dateCreate;
    private List<PrefResp> preferences;
    private Integer itineraryCount;
}
