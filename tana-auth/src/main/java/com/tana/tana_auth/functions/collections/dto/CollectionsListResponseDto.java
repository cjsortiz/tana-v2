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
public class CollectionsListResponseDto {

    private List<CollectionsResponseDto> natureAndScenery;
    private List<CollectionsResponseDto> foodAndDrink;
    private List<CollectionsResponseDto> communityAndCulture;
    private List<CollectionsResponseDto> sportsAndWellness;
    private CollectionsResponseDto featuredCollection;
    private List<SpotlightResponseDto> spotlightResponseDtoList;
}
