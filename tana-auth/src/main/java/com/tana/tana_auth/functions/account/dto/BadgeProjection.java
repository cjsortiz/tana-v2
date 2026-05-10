package com.tana.tana_auth.functions.account.dto;

public interface BadgeProjection {
    String getBadge();
    Long getCollectionId();
    String getCollectionName();
    Long getExplorerCount();
    Long getExploredSpotsCount();
    Long getTotalSpots();
    String getOverview();
    String getBadgeOverview();
}
