package com.tana.tana_auth.functions.qr.service;

import com.tana.tana_auth.functions.qr.dto.QrAnalyticsDto;
import jakarta.servlet.http.HttpServletRequest;

public interface QrService {
    String recordScanAndBuildHandoff(
        String type,
        Long targetId,
        String appUrl,
        HttpServletRequest request
    );
    QrAnalyticsDto getAnalytics();
}
