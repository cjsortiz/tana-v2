CREATE TABLE IF NOT EXISTS QrScanEvent (
    scanId BIGINT NOT NULL AUTO_INCREMENT,
    qrType VARCHAR(24) NOT NULL,
    targetId BIGINT NULL,
    scannerHash VARCHAR(64) NOT NULL,
    scanToken VARCHAR(36) NULL,
    accountId BIGINT NULL,
    platform VARCHAR(24) NULL,
    userAgent VARCHAR(512) NULL,
    scannedAt DATETIME(6) NOT NULL,
    claimedAt DATETIME(6) NULL,
    PRIMARY KEY (scanId),
    UNIQUE KEY uk_qr_scan_token (scanToken),
    KEY idx_qr_scan_type_target (qrType, targetId),
    KEY idx_qr_scan_scanned_at (scannedAt),
    KEY idx_qr_scan_account (accountId)
);

-- Existing installations already have QrScanEvent. Hibernate's configured
-- schema updater adds the three nullable claim columns and indexes safely.
