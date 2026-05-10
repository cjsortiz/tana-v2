package com.tana.tana_auth.functions.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMasterResponseDto {

    private String preferMood;
    private Boolean isOnboarded;

    private String firstName;
    private String lastName;
    private String userLocation;
    private String bio;
    private String preferLang;
    private String userType;
}
