package com.rotacultural.rcservice.dtos.favorites;

import com.rotacultural.rcservice.entities.enums.Price;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavResp {
    private Long userId;
    private Long locId;
    private LocalDateTime dateCreate;
    private String placeName;
    private String placeCity;
    private String placeState;
    private Price price;
    private String coverImage;
    private String categoryName;
}
