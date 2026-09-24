package com.tana.tana_auth.functions.places.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PlacesRequestDto {

    private Long placeId;

    private String name;

    private String overview;

    private String categoryTypeEnum;

    private List<String> subCategoryTypeEnum;

    private String town;

    private String googleAddress;

    private String facebook;

    private String instagram;

    private Boolean isTanaVerified;

    private List<String> imageStrings;

    private String gpsLocation;

    private String spotId;

    private String openingHours;

    private String openingDays;

    private String tanaTip;

    private String habalHabalTricycle;

    private String commute;

    private String walkFromDropOff;

    private String privateCarVan;

    private List<String> collections;


}
