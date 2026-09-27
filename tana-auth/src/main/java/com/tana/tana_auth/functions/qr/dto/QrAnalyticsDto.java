package com.tana.tana_auth.functions.qr.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QrAnalyticsDto {
    private long totalScans;
    private long uniqueScanners;
    private long eventScans;
    private long eventUniqueScanners;
    private long categoryScans;
    private long categoryUniqueScanners;
    private long collectionScans;
    private long collectionUniqueScanners;
    private long spotScans;
    private long spotUniqueScanners;
    private long routeScans;
    private long routeUniqueScanners;
    private long downloadScans;
    private long downloadUniqueScanners;
    private List<QrTargetAnalyticsDto> targets;
    private List<QrSourceAnalyticsDto> sources;
}
