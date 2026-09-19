package com.tana.tana_auth.functions.qr.repository;

import com.tana.tana_auth.functions.qr.model.QrScanEvent;
import com.tana.tana_auth.functions.qr.model.QrType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrScanEventRepository extends JpaRepository<QrScanEvent, Long> {
    Optional<QrScanEvent> findByScanToken(String scanToken);

    long countByQrType(QrType qrType);

    @Query("""
        select event.downloadPlatform as downloadPlatform, event.partnerId as partnerId, event.qrType as qrType,
               event.targetId as targetId, count(event.scanId) as scanCount
        from QrScanEvent event
        group by event.downloadPlatform, event.partnerId, event.qrType, event.targetId
        """)
    List<QrSourceScanProjection> findSourceAnalytics();

    @Query("select count(distinct event.scannerHash) from QrScanEvent event")
    long countUniqueScanners();

    @Query("select count(distinct event.scannerHash) from QrScanEvent event where event.qrType = :qrType")
    long countUniqueScannersByType(QrType qrType);

    @Query("""
        select event.qrType as qrType,
               event.targetId as targetId,
               count(event.scanId) as scanCount,
               count(distinct event.scannerHash) as uniqueScanners
        from QrScanEvent event
        where event.targetId is not null
        group by event.qrType, event.targetId
        order by count(event.scanId) desc
        """)
    List<QrTargetScanProjection> findTargetAnalytics();
}
