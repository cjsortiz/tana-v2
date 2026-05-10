package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotlightResponseDto {
    private Long placeId;
    private String name;
    private String town;
    private String overview;
    private String mainCategory;
    private String helperText;
    private Long collectionId;
    private String collectionName;
}
