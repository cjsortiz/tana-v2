package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorScanRequestDto {
    private String explorerQrToken;
    private Long spotId;
}
