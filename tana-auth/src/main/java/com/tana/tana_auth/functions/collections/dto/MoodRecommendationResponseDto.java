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
public class MoodRecommendationResponseDto {
    private Integer mood;
    private String emoji;
    private String name;
    private String description;
    private List<String> tags;
    private List<RouteResponseDto> routes;
}
