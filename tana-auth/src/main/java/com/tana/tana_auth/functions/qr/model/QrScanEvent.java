package com.tana.tana_auth.functions.qr.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "QrScanEvent",
    indexes = {
        @Index(name = "idx_qr_scan_type_target", columnList = "qrType,targetId"),
        @Index(name = "idx_qr_scan_scanned_at", columnList = "scannedAt"),
        @Index(name = "idx_qr_scan_account", columnList = "accountId")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class QrScanEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scanId")
    private Long scanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "qrType", nullable = false, length = 24)
    private QrType qrType;

    @Column(name = "targetId")
    private Long targetId;

    @Column(name = "partnerId")
    private Long partnerId;

    @Column(name = "downloadPlatform", length = 16)
    private String downloadPlatform;

    @Column(name = "scannerHash", nullable = false, length = 64)
    private String scannerHash;

    @Column(name = "scanToken", unique = true, length = 36)
    private String scanToken;

    @Column(name = "accountId")
    private Long accountId;

    @Column(name = "platform", length = 24)
    private String platform;

    @Column(name = "userAgent", length = 512)
    private String userAgent;

    @Column(name = "scannedAt", nullable = false)
    private LocalDateTime scannedAt;

    @Column(name = "claimedAt")
    private LocalDateTime claimedAt;
}
