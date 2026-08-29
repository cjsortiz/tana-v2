package com.tana.tana_auth.functions.login.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleTokenInfoDto {

    private String aud;

    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    private String name;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String picture;

    private String sub;
}
