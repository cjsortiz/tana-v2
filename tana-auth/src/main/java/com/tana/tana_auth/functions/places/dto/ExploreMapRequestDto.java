package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExploreMapRequestDto {

    private Long placeId;
    private Long collectionId;
    private String mainCategory;
    private String search;
    private Boolean nearbyOnly;
    private Boolean savedOnly;
    private Double latitude;
    private Double longitude;

}
