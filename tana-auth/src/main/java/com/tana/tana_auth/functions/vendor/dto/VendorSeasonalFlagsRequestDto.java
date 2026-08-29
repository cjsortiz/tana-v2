package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorSeasonalFlagsRequestDto {
    private Long spotId;
    private Boolean temporarilyClosed;
    private String closedFrom;
    private String closedTo;
    private String holidayHours;
    private String seasonalOffering;
}
