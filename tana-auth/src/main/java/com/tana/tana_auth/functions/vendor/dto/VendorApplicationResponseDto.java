package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorApplicationResponseDto {
    private Long applicationId;
    private String spotName;
    private String ownerName;
    private String phone;
    private String email;
    private String category;
    private String location;
    private String status;
    private Long linkedPlaceId;
    private String reviewNote;
}
