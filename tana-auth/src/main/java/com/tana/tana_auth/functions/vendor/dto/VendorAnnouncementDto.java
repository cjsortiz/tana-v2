package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorAnnouncementDto {
    private Long announcementId;
    private String title;
    private String body;
    private String date;
}
