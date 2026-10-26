package com.rotacultural.rcservice.dtos.itineraries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
