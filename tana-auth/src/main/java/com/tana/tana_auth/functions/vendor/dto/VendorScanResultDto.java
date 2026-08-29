package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorScanResultDto {
    private String status;
    private String explorerName;
    private String collectionName;
    private String progressLabel;
    private String message;
}
