package com.tana.tana_auth.functions.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDtoRequest {

    private String username;

    private String password;

    private String email;

    private String firstName;

    private String middleName;

    private String lastName;

    private String preferLang;

    private String preferMood;

    private String userType;
}
