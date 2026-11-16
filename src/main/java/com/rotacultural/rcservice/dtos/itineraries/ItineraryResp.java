package com.rotacultural.rcservice.dtos.itineraries;

import com.rotacultural.rcservice.entities.enums.Price;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResp {
    private long id;
    private long tripId;
    private LocalDate day;
    private long locId;
    private String description;
    private String placeName;
    private String placeAddress;
    private String placeCity;
    private String placeState;
    private String placeBusinessHours;
    private Price price;
    private BigDecimal estimatedCost;
    private String coverImage;
    private String categoryName;
}
