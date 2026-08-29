package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "VendorInvite",
    indexes = {
        @Index(name = "idx_vendor_invite_token", columnList = "token", unique = true),
        @Index(name = "idx_vendor_invite_email", columnList = "email")
    }
)
public class VendorInvite extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inviteId;

    @Column(name = "token", nullable = false, unique = true, length = 80)
    private String token;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "displayName")
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationId")
    private VendorApplication application;

    @Column(name = "expiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "acceptedAt")
    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptedAccountId")
    private AccountMaster acceptedAccount;

    @Column(name = "agreementAccepted")
    private Boolean agreementAccepted;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
