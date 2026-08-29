package com.tana.tana_auth.functions.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppleLoginRequestDto {

    @NotBlank
    private String identityToken;

    @NotBlank
    private String authorizationCode;

    @NotBlank
    private String nonce;
    private String appleUserId;
    private String fullName;
    private String userType;
}
