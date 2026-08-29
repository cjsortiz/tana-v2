package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VendorSpotDto {
    private Long spotId;
    private String name;
    private String category;
    private String town;
    private String image;
    private Integer visits;
    private Integer visitsThisWeek;
    private Integer reflections;
    private Integer photoUploads;
    private Integer saveCount;
    private String peakVisitLabel;
    private String status;
    private String overview;
    private String tanaTip;
    private String openingDays;
    private String openingHours;
    private String transportSummary;
    private List<String> includedCollections;
}
