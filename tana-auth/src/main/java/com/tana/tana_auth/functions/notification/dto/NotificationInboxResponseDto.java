package com.tana.tana_auth.functions.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInboxResponseDto {

    private Long notificationId;

    private String title;

    private String body;

    private String notificationType;

    private boolean read;

    private Map<String, Object> data;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
