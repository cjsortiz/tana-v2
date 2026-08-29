package com.tana.tana_common.model;

import com.tana.tana_common.util.converter.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "NotificationInbox",
    indexes = {
        @Index(name = "idx_notification_account_read", columnList = "accountId,isRead"),
        @Index(name = "idx_notification_account_created", columnList = "accountId,createdAt")
    }
)
public class NotificationInbox extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accountId", nullable = false)
    private AccountMaster account;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "notificationType", length = 64)
    private String notificationType;

    @Column(name = "isRead", columnDefinition = "TINYINT(1)")
    private boolean read;

    @Column(name = "data", columnDefinition = "JSON")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> data;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "readAt")
    private LocalDateTime readAt;
}
