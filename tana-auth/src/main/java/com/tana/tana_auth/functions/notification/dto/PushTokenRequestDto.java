package com.tana.tana_auth.functions.notification.dto;

import com.tana.tana_common.constant.enums.DevicePlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenRequestDto {

    private String token;

    private DevicePlatformEnum platform;

    private String deviceId;
}
