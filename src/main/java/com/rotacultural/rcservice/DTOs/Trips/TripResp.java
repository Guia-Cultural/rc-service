package com.rotacultural.rcservice.DTOs.Trips;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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


}
