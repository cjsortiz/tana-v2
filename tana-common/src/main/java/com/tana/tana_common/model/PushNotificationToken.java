package com.tana.tana_common.model;

import com.tana.tana_common.constant.enums.DevicePlatformEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "PushNotificationToken",
    indexes = {
        @Index(name = "idx_push_token_account", columnList = "accountId"),
        @Index(name = "idx_push_token_value", columnList = "token", unique = true)
    }
)
public class PushNotificationToken extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accountId", nullable = false)
    private AccountMaster account;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatformEnum platform;

    @Column(name = "deviceId", length = 128)
    private String deviceId;

    @Column(name = "enabled", columnDefinition = "TINYINT(1)")
    private boolean enabled;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
