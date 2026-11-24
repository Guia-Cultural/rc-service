package com.rotacultural.rcservice.dtos.recommendations;

import com.rotacultural.rcservice.dtos.places.LocResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedPlaceResp {
    private LocResp place;
    private int score;
    private boolean matchesPreference;
    private boolean withinBudget;
    private String reason;
}
