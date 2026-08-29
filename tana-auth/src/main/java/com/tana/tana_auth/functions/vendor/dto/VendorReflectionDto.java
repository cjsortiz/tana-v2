package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorReflectionDto {
    private Long reflectionId;
    private Long spotId;
    private String spotName;
    private String userName;
    private String content;
    private String date;
    private String image;
}
