package com.rotacultural.rcservice.dtos.itineraries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateItineraryReq {
    private List<Long> placeIds;
    private Integer maxPerDay;
}
