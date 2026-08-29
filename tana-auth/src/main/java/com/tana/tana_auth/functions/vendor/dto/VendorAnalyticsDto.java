package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VendorAnalyticsDto {
    private Integer totalVisitedMarks;
    private Integer visitedChangePct;
    private String peakVisitWindow;
    private Integer peakVisitCount;
    private Integer totalUserImages;
    private Integer totalReflections;
    private String topSpotName;
    private Integer topSpotVisits;
    private List<Integer> weeklyVisits;
    private List<VendorUserImageDto> recentImages;
    private List<VendorReflectionDto> recentReflections;
    private Integer uniqueExplorers;
    private Integer explorerChangePct;
    private String busiestDay;
    private String busiestTime;
    private List<VendorCollectionInsightDto> collectionInsights;
    private List<VendorAnnouncementDto> announcements;
    private List<VendorActivityItemDto> activityItems;
}
