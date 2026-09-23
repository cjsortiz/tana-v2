package com.tana.tana_auth.functions.routes.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CompletedRouteResponseDto {
    private Long routeId;
    private LocalDateTime completedAt;
    private LocalDateTime restartedAt;
    private boolean retaking;
    private java.util.List<String> visitedPlaceIds;
}
