package com.rotacultural.rcservice.dtos.places;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceImageResp {
    private Long id;
    private Long placeId;
    private String url;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private Boolean cover;
    private LocalDateTime dateCreate;
}
