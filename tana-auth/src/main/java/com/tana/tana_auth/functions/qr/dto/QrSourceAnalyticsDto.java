package com.tana.tana_auth.functions.qr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrSourceAnalyticsDto {
    private String downloadPlatform;
    private Long partnerId;
    private String type;
    private Long targetId;
    private long scanCount;
}
