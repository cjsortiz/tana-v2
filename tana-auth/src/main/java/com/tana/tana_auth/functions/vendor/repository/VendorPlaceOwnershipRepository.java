package com.tana.tana_auth.functions.vendor.repository;

import com.tana.tana_common.model.VendorPlaceOwnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPlaceOwnershipRepository extends JpaRepository<VendorPlaceOwnership, Long> {

    @Query("""
    SELECT ownership
    FROM VendorPlaceOwnership ownership
    JOIN FETCH ownership.place place
    WHERE ownership.vendorAccount.id = :vendorAccountId
      AND LOWER(COALESCE(ownership.status, 'active')) = 'active'
    ORDER BY place.name ASC
    """)
    List<VendorPlaceOwnership> findActiveByVendorAccountId(@Param("vendorAccountId") Long vendorAccountId);

    @Query("""
    SELECT ownership
    FROM VendorPlaceOwnership ownership
    JOIN FETCH ownership.vendorAccount vendor
    JOIN FETCH ownership.place place
    WHERE LOWER(COALESCE(ownership.status, 'active')) = 'active'
    ORDER BY vendor.email ASC, place.name ASC
    """)
    List<VendorPlaceOwnership> findAllActiveOwnerships();

    @Query("""
    SELECT ownership
    FROM VendorPlaceOwnership ownership
    WHERE ownership.vendorAccount.id = :vendorAccountId
      AND ownership.place.id = :placeId
      AND LOWER(COALESCE(ownership.status, 'active')) = 'active'
    """)
    Optional<VendorPlaceOwnership> findActiveOwnership(
        @Param("vendorAccountId") Long vendorAccountId,
        @Param("placeId") Long placeId
    );

    boolean existsByVendorAccountId(Long vendorAccountId);
}
