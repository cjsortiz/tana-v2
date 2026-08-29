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
public class PushNotificationRequestDto {

    private Long accountId;

    private String title;

    private String body;

    private Map<String, String> data;
}
