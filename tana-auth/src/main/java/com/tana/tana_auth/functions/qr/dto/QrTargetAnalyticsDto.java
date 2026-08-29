package com.tana.tana_auth.functions.qr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrTargetAnalyticsDto {
    private String type;
    private Long targetId;
    private long scanCount;
    private long uniqueScanners;
}
