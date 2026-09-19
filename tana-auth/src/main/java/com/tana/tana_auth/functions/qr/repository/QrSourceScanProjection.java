package com.tana.tana_auth.functions.qr.repository;

import com.tana.tana_auth.functions.qr.model.QrType;

public interface QrSourceScanProjection {
    String getDownloadPlatform();
    Long getPartnerId();
    QrType getQrType();
    Long getTargetId();
    long getScanCount();
}
