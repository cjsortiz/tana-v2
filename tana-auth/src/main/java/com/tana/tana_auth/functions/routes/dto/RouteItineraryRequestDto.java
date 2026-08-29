package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RouteItineraryRequestDto {
    private Long routeId;
    private Long placeId;
    private Integer dayNumber;
    private Integer stopOrder;
    private String timeBlock;
    private String priority;
    private String details;
}
