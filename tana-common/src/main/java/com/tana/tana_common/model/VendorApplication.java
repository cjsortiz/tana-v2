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
    name = "VendorApplication",
    indexes = {
        @Index(name = "idx_vendor_application_status", columnList = "status"),
        @Index(name = "idx_vendor_application_email", columnList = "email")
    }
)
public class VendorApplication extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(name = "spotName", nullable = false)
    private String spotName;

    @Column(name = "ownerName", nullable = false)
    private String ownerName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "worthVisiting", columnDefinition = "TEXT")
    private String worthVisiting;

    @Column(name = "operatingHours", columnDefinition = "TEXT")
    private String operatingHours;

    @Column(name = "habalHabalPickup")
    private String habalHabalPickup;

    @Column(name = "commuteRoute", columnDefinition = "TEXT")
    private String commuteRoute;

    @Column(name = "walkTrekInfo", columnDefinition = "TEXT")
    private String walkTrekInfo;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linkedPlaceId")
    private PlaceMaster linkedPlace;

    @Column(name = "reviewNote", columnDefinition = "TEXT")
    private String reviewNote;
}
