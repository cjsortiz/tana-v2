package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedSpotDetailsResponseDto {

    private Long placeId;

    private String name;

    private String collectionName;

    private Integer isVisited;

    private String collectionImage;

}
