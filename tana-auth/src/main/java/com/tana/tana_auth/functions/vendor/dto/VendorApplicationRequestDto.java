package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorApplicationRequestDto {
    private String spotName;
    private String ownerName;
    private String phone;
    private String email;
    private String category;
    private String location;
    private String worthVisiting;
    private String operatingHours;
    private String habalHabalPickup;
    private String commuteRoute;
    private String walkTrekInfo;
}
