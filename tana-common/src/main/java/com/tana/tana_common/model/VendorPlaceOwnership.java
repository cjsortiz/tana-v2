package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "VendorPlaceOwnership",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vendorAccountId", "placeId"})
    },
    indexes = {
        @Index(name = "idx_vendor_place_vendor", columnList = "vendorAccountId"),
        @Index(name = "idx_vendor_place_place", columnList = "placeId")
    }
)
public class VendorPlaceOwnership extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendorAccountId", nullable = false)
    private AccountMaster vendorAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

    @Column(name = "role", length = 32)
    private String role;

    @Column(name = "status", length = 32)
    private String status;
}
