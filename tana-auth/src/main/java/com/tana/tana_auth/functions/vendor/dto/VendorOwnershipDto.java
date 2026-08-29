package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorOwnershipDto {
    private Long ownershipId;
    private Long vendorAccountId;
    private Long placeId;
    private String placeName;
    private String role;
    private String status;
}
