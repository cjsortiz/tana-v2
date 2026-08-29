package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorInviteResponseDto {
    private Long inviteId;
    private String token;
    private String email;
    private String displayName;
    private Long placeId;
    private String placeName;
    private String status;
    private String expiresAt;
    private Boolean expired;
}
