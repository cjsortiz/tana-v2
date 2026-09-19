package com.tana.tana_auth.functions.qr.controller;

import com.tana.tana_auth.functions.qr.service.QrService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.TEXT_HTML)
            .body(qrService.recordScanAndBuildHandoff("download", null, appUrl, request));
    }

    @GetMapping(value = "/open/download/logo", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> downloadLogo() {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .body(new ClassPathResource("templates/qr-download-logo.png"));
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

    @PostMapping("/scans/{scanToken}/claim")
    public TanaApiResponse claimScan(@PathVariable String scanToken) {
        qrService.claimScan(scanToken);
        return TanaApiResponse.builder().isSuccess(true).build();
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
