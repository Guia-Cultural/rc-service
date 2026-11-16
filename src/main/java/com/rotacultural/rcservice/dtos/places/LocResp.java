package com.rotacultural.rcservice.dtos.places;

import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocResp {
    private Long id;
    private String name;
    private String description;
    private Long catId;
    private String categoryName;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String businessHours;
    private Price price;
    private BigDecimal estimatedCost;
    private String coverImage;
    private String officialWebsite;
    private String ticketLink;
    private String contact;
    private Long userId;
    private String creatorName;
    private Status status;
    private String rejectionReason;
    private LocalDateTime dateCreate;
    private List<PlaceImageResp> images;
    private Double averageRating;
    private Integer reviewCount;
}
