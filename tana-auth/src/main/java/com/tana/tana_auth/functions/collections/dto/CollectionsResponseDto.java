package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionsResponseDto {

    private Long collectionId;

    private String collectionName;

    private Long spotCounts;

    private Long category;

    private Long visitedCount;

    private String helperText;

    private String collectionImage;
}
