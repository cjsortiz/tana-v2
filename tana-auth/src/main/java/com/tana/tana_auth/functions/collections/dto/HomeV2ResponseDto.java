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
public class HomeV2ResponseDto {
    private List<SpotlightResponseDto> trendingSpots;
    private List<RouteResponseDto> trendingRoutes;
    private List<SpotlightResponseDto> weeklyTrendingSpots;
    private List<CollectionsResponseDto> collectionPreview;
    private MoodRecommendationResponseDto recommendedForYou;
    private List<RouteResponseDto> panglaoAirportRoutes;
    private List<RouteResponseDto> routes;
}
