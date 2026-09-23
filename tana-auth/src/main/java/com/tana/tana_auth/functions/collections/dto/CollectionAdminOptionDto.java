package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CollectionAdminOptionDto {
    private Long collectionId;
    private String collectionName;
    private String segment;
    private String collectionImage;
    private String badgeImage;
    private String overview;
    private String badge;
    private String badgeOverview;
    private String helperText;
    private Integer moodType;
    private Integer moodPriority;
    private java.util.List<CollectionSpotRequestDto> spots;
}
