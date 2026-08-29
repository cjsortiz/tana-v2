package com.tana.tana_auth.functions.qr.controller;

import com.tana.tana_auth.functions.qr.service.QrService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
public class QrController {
    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping(value = "/open/download", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> openDownload(
        @RequestParam(required = false) String appUrl,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(qrService.recordScanAndBuildHandoff("download", null, appUrl, request));
    }

    @GetMapping(value = "/open/{type}/{targetId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> openTarget(
        @PathVariable String type,
        @PathVariable Long targetId,
        @RequestParam(required = false) String appUrl,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(qrService.recordScanAndBuildHandoff(type, targetId, appUrl, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/analytics")
    public TanaApiResponse getAnalytics() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(qrService.getAnalytics())
            .build();
    }
}
