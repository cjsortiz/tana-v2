package com.tana.tana_auth.functions.places.dto;

import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacesDetailsResponseDto {

    private Long id;
    private String name;
    private String overview;
    private String latitude;
    private String longitude;
    private Double distanceKm;
    private boolean isVisited;
    private List<String> subCategories;
    private int visitCount;
    private Long collectionId;
    private String collectionName;
    private String collectionImage;
    private String badge;
    private String badgeDesc;
    private Integer index;
    private String tanaTip;

    private String habalHabalTricycle;

    private String commute;

    private String walkFromDropOff;

    private String privateCarVan;
    private String openingHours;
    private String openingDays;
    private List<String> spotImages;
    private Boolean saved;
    private String mainCategory;
}
