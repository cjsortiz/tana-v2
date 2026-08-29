package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorDetailsDto {
    private Long vendorId;
    private String businessName;
    private String ownerName;
    private String category;
    private String status;
    private String location;
    private String contactEmail;
    private String contactNumber;
    private Double rating;
    private Integer totalSpots;
    private Integer totalVisits;
    private Integer totalReflections;
    private String coverImage;
}
