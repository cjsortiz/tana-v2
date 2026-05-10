package com.tana.tana_auth.functions.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBasicDetailsDto {
    private Long accountId;
    private String displayName;
    private Long spotVisitedCount;
    private Long collectionExploredCount;
    private Long badgeEarnedCount;
    private Integer userType;
    private String userImage;
    private String firstName;
    private String lastName;
    private String userLocation;
    private String bio;
}
