package com.tana.tana_auth.functions.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInboxRequestDto {

    private String title;

    private String body;

    private String notificationType;

    private Map<String, Object> data;
}
