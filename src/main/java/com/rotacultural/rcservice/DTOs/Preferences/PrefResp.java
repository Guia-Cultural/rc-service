package com.rotacultural.rcservice.DTOs.Preferences;

import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Trips;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrefResp {
    private Long id;
    private Trips trips;
    private Categories categories;
}
