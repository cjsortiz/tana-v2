package com.tana.tana_auth.functions.qr.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.qr.dto.QrAnalyticsDto;
import com.tana.tana_auth.functions.qr.dto.QrSourceAnalyticsDto;
import com.tana.tana_auth.functions.routes.repository.RoutePartnerRepository;
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
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class QrServiceImpl implements QrService {
    private final QrScanEventRepository qrScanEventRepository;
    private final CollectionRepository collectionRepository;
    private final PlacesRepository placesRepository;
    private final RouteRepository routeRepository;
    private final AuthConfig authConfig;
    private final RoutePartnerRepository routePartnerRepository;

    @Value("${qr.ios-app-store-url:https://apps.apple.com/ph/app/tana/id6769938553}")
    private String iosAppStoreUrl;

    @Value("${qr.android-play-store-url:https://play.google.com/store/apps/details?id=com.app.tana}")
    private String androidPlayStoreUrl;

    @Value("${qr.android-download-available:false}")
    private boolean androidDownloadAvailable;

    @Value("${qr.analytics-hash-salt:tana-qr-analytics}")
    private String analyticsHashSalt;

    public QrServiceImpl(
        QrScanEventRepository qrScanEventRepository,
        CollectionRepository collectionRepository,
        PlacesRepository placesRepository,
        RouteRepository routeRepository,
        AuthConfig authConfig,
        RoutePartnerRepository routePartnerRepository
    ) {
        this.qrScanEventRepository = qrScanEventRepository;
        this.collectionRepository = collectionRepository;
        this.placesRepository = placesRepository;
        this.routeRepository = routeRepository;
        this.authConfig = authConfig;
        this.routePartnerRepository = routePartnerRepository;
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

        Long partnerId = resolvePartnerId(request.getParameter("partnerId"));
        String downloadPlatform = qrType == QrType.DOWNLOAD
            ? request.getParameter("platform") : null;
        if (qrType == QrType.DOWNLOAD && downloadPlatform == null) downloadPlatform = "android";
        if (downloadPlatform != null && !downloadPlatform.equals("android") && !downloadPlatform.equals("ios")) {
            throw new ResponseStatusException(NOT_FOUND, "QR download platform not found");
        }

        String userAgent = safeHeader(request.getHeader("User-Agent"));
        QrScanEvent event = new QrScanEvent();
        event.setQrType(qrType);
        event.setTargetId(targetId);
        event.setPartnerId(partnerId);
        event.setDownloadPlatform(downloadPlatform);
        event.setScannerHash(hashScanner(request, userAgent));
        event.setScanToken(UUID.randomUUID().toString());
        event.setPlatform(detectPlatform(userAgent));
        event.setUserAgent(truncate(userAgent, 512));
        event.setScannedAt(LocalDateTime.now());
        qrScanEventRepository.save(event);

        return qrType == QrType.DOWNLOAD
            ? ("ios".equals(downloadPlatform) ? iosDownloadPage() : downloadPage())
            : handoffPage(qrType, targetId, event.getScanToken(), appUrl);
    }

    @Override
    @Transactional
    public void claimScan(String scanToken) {
        QrScanEvent event = qrScanEventRepository.findByScanToken(scanToken)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "QR scan not found"));
        Long currentAccountId = authConfig.getCurrentUserId();
        if (event.getAccountId() != null && !event.getAccountId().equals(currentAccountId)) {
            throw new ResponseStatusException(NOT_FOUND, "QR scan not found");
        }
        if (event.getAccountId() == null) {
            event.setAccountId(currentAccountId);
            event.setClaimedAt(LocalDateTime.now());
            qrScanEventRepository.save(event);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QrAnalyticsDto getAnalytics() {
        return QrAnalyticsDto.builder()
            .sources(qrScanEventRepository.findSourceAnalytics().stream()
                .map(row -> QrSourceAnalyticsDto.builder()
                    .downloadPlatform(row.getDownloadPlatform())
                    .partnerId(row.getPartnerId())
                    .type(row.getQrType().name())
                    .targetId(row.getTargetId())
                    .scanCount(row.getScanCount())
                    .build())
                .toList())
            .totalScans(qrScanEventRepository.count())
            .uniqueScanners(qrScanEventRepository.countUniqueScanners())
            .collectionScans(qrScanEventRepository.countByQrType(QrType.COLLECTION))
            .collectionUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.COLLECTION))
            .spotScans(qrScanEventRepository.countByQrType(QrType.SPOT))
            .spotUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.SPOT))
            .routeScans(qrScanEventRepository.countByQrType(QrType.ROUTE))
            .routeUniqueScanners(qrScanEventRepository.countUniqueScannersByType(QrType.ROUTE))
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

    private Long resolvePartnerId(String value) {
        if (value == null) return null;
        try {
            Long partnerId = Long.valueOf(value);
            if (partnerId > 0 && routePartnerRepository.existsById(partnerId)) return partnerId;
        } catch (NumberFormatException ignored) {
            // Invalid partner links must not silently count as general scans.
        }
        throw new ResponseStatusException(NOT_FOUND, "QR partner not found");
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

    private String handoffPage(QrType type, Long targetId, String scanToken, String requestedAppUrl) {
        String typeValue = type.name().toLowerCase(Locale.ROOT);
        String targetQuery = targetId == null ? "" : "&id=" + targetId;
        String scanQuery = "&scan=" + scanToken;
        boolean expoGoTarget = isExpoGoUrl(requestedAppUrl);
        String deepLink = expoGoTarget
            ? appendQuery(requestedAppUrl, "scan", scanToken)
            : "tanav2://qr-open?type=" + typeValue + targetQuery + scanQuery;
        String androidIntent = "intent://qr-open?type=" + typeValue + targetQuery + scanQuery
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

    private String iosDownloadPage() {
        String url = org.springframework.web.util.HtmlUtils.htmlEscape(iosAppStoreUrl);
        return """
            <!doctype html><html lang="en"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <meta http-equiv="refresh" content="0;url=%s">
            <title>tana! on the App Store</title></head>
            <body><h1>tana! for iOS</h1><p><a href="%s">Download on the App Store</a></p></body></html>
            """.formatted(url, url);
    }

    private String downloadPage() {
        String storeUrl = androidPlayStoreUrl == null ? "" : androidPlayStoreUrl.trim();
        boolean available = androidDownloadAvailable
            && storeUrl.startsWith("https://play.google.com/store/apps/details?id=");
        String escapedUrl = org.springframework.web.util.HtmlUtils.htmlEscape(storeUrl);
        String redirect = available
            ? "<meta http-equiv=\"refresh\" content=\"0;url=" + escapedUrl + "\">"
            : "";
        String action = available
            ? "<a href=\"" + escapedUrl + "\">Get it on Google Play</a>"
            : "<p class=\"note\">A little more time. A lot more to discover.</p>";
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <meta name="robots" content="noindex">
              <meta name="theme-color" content="#0c4230">
              %s
              <title>tana! for Android</title>
              <style>
                *{box-sizing:border-box}body{margin:0;background:#fff;color:#123d30;font-family:system-ui,-apple-system,sans-serif;letter-spacing:0}
                main{min-height:90svh;max-width:640px;margin:auto;padding:64px 24px 40px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center}
                img{width:112px;height:112px;object-fit:contain}h1{font-size:48px;line-height:1.1;margin:24px 0 12px}h2{font-size:26px;line-height:1.25;margin:0 0 20px}
                p{font-size:17px;line-height:1.6;max-width:400px;margin:0;color:#53625c}.platform{font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:0;color:#54712a;margin:20px 0 12px}
                .note{font-size:14px;margin-top:32px;color:#6c706d}a{display:inline-block;max-width:100%%;margin-top:28px;padding:15px 22px;border-radius:8px;background:#0c4230;color:#fff;text-decoration:none;font-weight:700}a:focus-visible{outline:3px solid #b6ca67;outline-offset:4px}
                footer{min-height:10svh;border-top:1px solid #e5e9e5;padding:24px;text-align:center;font-size:13px;color:#53625c}
              </style>
            </head>
            <body><main>
              <img src="./download/logo" alt="" width="112" height="112">
              <h1>tana!</h1><p class="platform">For Android</p>
              <h2>%s</h2><p>%s</p>%s
            </main><footer>Made for discovering Bohol.</footer></body>
            </html>
            """.formatted(redirect,
                available ? "Your next adventure is ready." : "Coming soon.",
                available ? "Download tana! on Google Play and start exploring Bohol."
                    : "Discover local spots, follow new routes, and find your next adventure in Bohol. tana! is coming to Google Play.",
                action);
    }

    private String appendQuery(String url, String name, String value) {
        if (url == null || url.isBlank()) return url;
        return url + (url.contains("?") ? "&" : "?") + name + "=" + encodeUrl(value);
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
