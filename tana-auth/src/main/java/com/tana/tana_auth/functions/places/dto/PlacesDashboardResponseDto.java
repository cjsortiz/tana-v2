package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlacesDashboardResponseDto {

    private List<DashboardImageResponse> trendingPlaces;

    private List<DashboardImageResponse> nearby;
}

