package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorUserImageDto {
    private Long imageId;
    private Long spotId;
    private String spotName;
    private String userName;
    private String image;
    private String date;
}
