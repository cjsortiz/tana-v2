package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RouteResponseDto {
    private Long routeId;
    private String routeName;
    private RouteCategoryResponseDto category;
    private RoutePartnerResponseDto partner;
    private String routeDuration;
    private String overview;
    private String helperText;
    private String routeImage;
    private List<String> tags;
    private List<RouteItineraryResponseDto> itineraries;
}
