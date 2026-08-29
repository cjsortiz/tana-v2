package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminVendorDto {
    private Long vendorAccountId;
    private String vendorName;
    private String email;
    private Long ownershipId;
    private Long placeId;
    private String placeName;
    private String role;
    private String status;
}
