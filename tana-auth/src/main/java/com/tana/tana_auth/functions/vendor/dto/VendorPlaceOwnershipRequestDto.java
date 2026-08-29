package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorPlaceOwnershipRequestDto {
    private Long vendorAccountId;
    private Long placeId;
    private String role;
    private String status;
}
