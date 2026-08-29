package com.tana.tana_auth.functions.qr.service.impl;

import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.qr.dto.QrAnalyticsDto;
import com.tana.tana_auth.functions.qr.dto.QrTargetAnalyticsDto;
import com.tana.tana_auth.functions.qr.model.QrScanEvent;
import com.tana.tana_auth.functions.qr.model.QrType;
import com.tana.tana_auth.functions.qr.repository.QrScanEventRepository;
import com.tana.tana_auth.functions.qr.service.QrService;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class QrServiceImpl implements QrService {
    private final QrScanEventRepository qrScanEventRepository;
    private final CollectionRepository collectionRepository;
    private final PlacesRepository placesRepository;
    private final RouteRepository routeRepository;

    @Value("${qr.ios-app-store-url:https://apps.apple.com/ph/search?term=tana}")
    private String iosAppStoreUrl;

    @Value("${qr.android-play-store-url:https://play.google.com/store/apps/details?id=com.app.tana}")
    private String androidPlayStoreUrl;

    @Value("${qr.analytics-hash-salt:tana-qr-analytics}")
    private String analyticsHashSalt;

    public QrServiceImpl(
        QrScanEventRepository qrScanEventRepository,
        CollectionRepository collectionRepository,
        PlacesRepository placesRepository,
        RouteRepository routeRepository
    ) {
        this.qrScanEventRepository = qrScanEventRepository;
        this.collectionRepository = collectionRepository;
        this.placesRepository = placesRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    @Transactional
    public String recordScanAndBuildHandoff(
        String type,
        Long targetId,
        String appUrl,
        HttpServletRequest request
    ) {
        QrType qrType = parseType(type);
        validateTarget(qrType, targetId);

        String userAgent = safeHeader(request.getHeader("User-Agent"));
        QrScanEvent event = new QrScanEvent();
        event.setQrType(qrType);
        event.setTargetId(targetId);
        event.setScannerHash(hashScanner(request, userAgent));
        event.setPlatform(detectPlatform(userAgent));
        event.setUserAgent(truncate(userAgent, 512));
        event.setScannedAt(LocalDateTime.now());
        qrScanEventRepository.save(event);

        return handoffPage(qrType, targetId, appUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public QrAnalyticsDto getAnalytics() {
        return QrAnalyticsDto.builder()
            .totalScans(qrScanEventRepository.count())
            .uniqueScanners(qrScanEventRepository.countUniqueScanners())
            .collectionScans(qrScanEventRepository.countByQrType(QrType.COLLECTION))
            .collectionUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.COLLECTION))
            .spotScans(qrScanEventRepository.countByQrType(QrType.SPOT))
            .spotUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.SPOT))
            .downloadScans(qrScanEventRepository.countByQrType(QrType.DOWNLOAD))
            .downloadUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.DOWNLOAD))
            .targets(qrScanEventRepository.findTargetAnalytics().stream()
                .map(row -> QrTargetAnalyticsDto.builder()
                    .type(row.getQrType().name())
                    .targetId(row.getTargetId())
                    .scanCount(row.getScanCount())
                    .uniqueScanners(row.getUniqueScanners())
                    .build())
                .toList())
            .build();
    }

    private QrType parseType(String type) {
        try {
            return QrType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(NOT_FOUND, "QR target not found");
        }
    }

    private void validateTarget(QrType type, Long targetId) {
        boolean valid = switch (type) {
            case DOWNLOAD -> targetId == null;
            case COLLECTION -> targetId != null && collectionRepository.existsById(targetId);
            case SPOT -> targetId != null && placesRepository.existsById(targetId);
            case ROUTE -> targetId != null && routeRepository.existsById(targetId);
        };
        if (!valid) throw new ResponseStatusException(NOT_FOUND, "QR target not found");
    }

    private String hashScanner(HttpServletRequest request, String userAgent) {
        String forwarded = safeHeader(request.getHeader("X-Forwarded-For"));
        String address = forwarded.isBlank()
            ? safeHeader(request.getRemoteAddr())
            : forwarded.split(",")[0].trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                (analyticsHashSalt + "|" + address + "|" + userAgent).getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash QR scanner", exception);
        }
    }

    private String detectPlatform(String userAgent) {
        String lower = userAgent.toLowerCase(Locale.ROOT);
        if (lower.contains("android")) return "ANDROID";
        if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ipod")) return "IOS";
        return "OTHER";
    }

    private String handoffPage(QrType type, Long targetId, String requestedAppUrl) {
        String typeValue = type.name().toLowerCase(Locale.ROOT);
        String targetQuery = targetId == null ? "" : "&id=" + targetId;
        boolean expoGoTarget = isExpoGoUrl(requestedAppUrl);
        String deepLink = expoGoTarget
            ? requestedAppUrl
            : "tanav2://qr-open?type=" + typeValue + targetQuery;
        String androidIntent = "intent://qr-open?type=" + typeValue + targetQuery
            + "#Intent;scheme=tanav2;package=com.app.tana;"
            + "S.browser_fallback_url=" + encodeUrl(androidPlayStoreUrl) + ";end";

        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <title>Open tana!</title>
              <style>
                body{font-family:system-ui,-apple-system,sans-serif;margin:0;min-height:100vh;display:grid;place-items:center;background:#f7f7f2;color:#123d30}
                main{text-align:center;padding:32px;max-width:420px}h1{font-size:28px;margin:0 0 8px}p{line-height:1.5;color:#56635e}
                a{display:inline-block;margin-top:16px;padding:13px 20px;background:#123d30;color:#fff;text-decoration:none;border-radius:8px;font-weight:700}
              </style>
            </head>
            <body><main><h1>Opening tana!</h1><p>Taking you to the requested place in the app.</p><a id="open-app">Open tana!</a></main>
            <script>
              (function(){
                var ua=navigator.userAgent||'';
                var android=/Android/i.test(ua);
                var ios=/iPhone|iPad|iPod/i.test(ua);
                var deepLink='%s';
                var androidIntent='%s';
                var fallback=android?'%s':'%s';
                var expoGo=%s;
                var target=expoGo?deepLink:(android?androidIntent:deepLink);
                var timer;
                function openApp(){
                  clearTimeout(timer);
                  window.location.href=target;
                  timer=setTimeout(function(){window.location.replace(fallback);},1800);
                }
                document.getElementById('open-app').href=target;
                document.getElementById('open-app').addEventListener('click',function(event){event.preventDefault();openApp();});
                document.addEventListener('visibilitychange',function(){if(document.hidden)clearTimeout(timer);});
                if(android||ios){setTimeout(openApp,120);}
              })();
            </script></body></html>
            """.formatted(
                escapeJs(deepLink),
                escapeJs(androidIntent),
                escapeJs(androidPlayStoreUrl),
                escapeJs(iosAppStoreUrl),
                expoGoTarget
            );
    }

    private boolean isExpoGoUrl(String value) {
        if (value == null || value.isBlank()) return false;
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("exp://") || lower.startsWith("exps://");
    }

    private String encodeUrl(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeJs(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'")
            .replace("<", "\\u003c").replace(">", "\\u003e");
    }

    private String safeHeader(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
