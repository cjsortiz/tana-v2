package com.tana.tana_auth.functions.events.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventNearbySpotDto {
    private Long placeId;
    private String name;
    private String town;
    private String mainCategory;
    private String distanceLabel;
    private String image;
}
