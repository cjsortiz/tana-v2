package com.tana.tana_auth.functions.login.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tana.tana_common.model.DropdownMaster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /**
     * Represents the type of token.
     */
    @JsonProperty("type")
    private static final String TYPE = "Bearer";

    /**
     * The actual token itself.
     */
    private String token;

    private String refreshToken;

    /**
     * Represents the ID of the user.
     */
    private long id;

    private String username;

    private Date passwordExpiry;


    private String image;

    private String image64;

    private String displayName;

    private boolean otpRequired;

    private String headerString;

    private boolean adminAccess;

    private String accessLevel;

    private List<DropdownMaster> dropdownMasterList;

    @JsonProperty("isOnboarded")
    private boolean isOnboarded;

}