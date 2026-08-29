package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RouteItineraryResponseDto {
    private Long routeItineraryId;
    private Long routeId;
    private String routeName;
    private String routeCategoryName;
    private Long placeId;
    private String placeName;
    private Integer dayNumber;
    private Integer stopOrder;
    private String timeBlock;
    private String priority;
    private String details;
    private Boolean visited;
    private String spotImage;
    private List<String> spotImages;
    private Long collectionId;
    private String collectionName;
    private String badge;
}
