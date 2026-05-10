package com.tana.tana_auth.functions.collections.dto;

import com.tana.tana_auth.functions.places.dto.PlacesDetailsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailsResponseDto {

    private String collectionId;
    private Boolean saved;
    private String collectionName;
    private List<String> subCategories;
    private String overView;
    private String badge;
    private Long exploredCount;
    private String collectionImage;
    private List<PlacesDetailsResponseDto> placeList;
}
