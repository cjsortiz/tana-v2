package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CollectionCreateRequestDto {
    private String collectionName;
    private String overview;
    private String badge;
    private String badgeOverview;
    private String helperText;
    private String collectionImage;
    private String segment;
    private Integer moodType;
    private Integer moodPriority;
}
