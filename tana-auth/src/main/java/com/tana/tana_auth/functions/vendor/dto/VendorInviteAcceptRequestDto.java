package com.tana.tana_auth.functions.vendor.dto;

import lombok.Data;

@Data
public class VendorInviteAcceptRequestDto {
    private String password;
    private String displayName;
    private Boolean agreementAccepted;
}
