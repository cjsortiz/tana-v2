package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponseDto {
    private String routeId;
    private String name;
    private String duration;
    private String pace;
    private String base;
    private String mix;
    private String badge;
    private String icon;
    private String blurb;
    private String description;
    private Boolean airportPartner;
    private List<String> tags;
    private List<RouteStopResponseDto> stops;
    private Long visitCount;
}
