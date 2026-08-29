package com.tana.tana_auth.functions.events.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventResponseDto {
    private Long id;
    private String eventSlug;
    private String eventName;
    private String category;
    private String dateLabel;
    private String monthSort;
    private String monthKey;
    private String location;
    private String gps;
    private String frequency;
    private String about;
    private String tanaTip;
    private String badge;
    private String website;
    private String hiddenTags;
    private String accentColor;
    private String tagBg;
    private String tagTextColor;
    private Boolean featured;
    private List<EventNearbySpotDto> nearbySpots;
}
