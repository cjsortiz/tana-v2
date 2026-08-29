package com.tana.tana_auth.functions.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class BadgeResponseDto {
    private String badge;
    private Long collectionId;
    private String collectionName;
    private String collectionImage;
    private Long explorerCount;
    private Long exploredSpotsCount;
    private Long totalSpots;
    private String overview;
    private String badgeOverview;
    private List<BadgeSpotsDto> spots;
    private String spotId;


    public BadgeResponseDto(String badge, Long collectionId, String collectionName, String collectionImage, Long explorerCount,
                            Long exploredSpotsCount, Long totalSpots, String overview, String badgeOverview,
                            List<BadgeSpotsDto> spots, String spotId) {
        this.badge = badge;
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.collectionImage = collectionImage;
        this.explorerCount = explorerCount;
        this.exploredSpotsCount = exploredSpotsCount;
        this.totalSpots = totalSpots;
        this.overview = overview;
        this.badgeOverview = badgeOverview;
        this.spots = spots;
        this.spotId = spotId;
    }
}
