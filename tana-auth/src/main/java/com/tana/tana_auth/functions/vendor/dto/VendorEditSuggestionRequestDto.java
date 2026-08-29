package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorEditSuggestionRequestDto {
    private Long spotId;
    private String field;
    private String message;
    private Boolean urgent;
}
