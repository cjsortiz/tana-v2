package com.tana.tana_auth.functions.routes.service;

import com.tana.tana_auth.functions.routes.dto.CompletedRouteResponseDto;
import com.tana.tana_common.constant.exception.TanaException;

import java.util.List;

public interface RouteCompletionService {
    List<CompletedRouteResponseDto> getCompletedRoutes();
    CompletedRouteResponseDto completeRoute(Long routeId) throws TanaException;
}
