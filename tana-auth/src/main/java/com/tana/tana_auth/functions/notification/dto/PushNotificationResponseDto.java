package com.tana.tana_auth.functions.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResponseDto {

    private int requestedCount;

    private int successCount;

    private int failureCount;
}
