package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorActivityItemDto {
    private Long activityId;
    private String type;
    private String explorerName;
    private String spotName;
    private String label;
    private String happenedAt;
    private Boolean muted;
}
