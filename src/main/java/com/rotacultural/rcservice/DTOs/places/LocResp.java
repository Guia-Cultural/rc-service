package com.rotacultural.rcservice.DTOs.places;

import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocResp {
    private Long id;
    private String name;
    private String description;
    private Long catId;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String businessHours;
    private Price price;
    private Long userId;
    private Status status;
    private LocalDateTime dateCreate;
}
