package com.tana.tana_auth.functions.routes.controller;

import com.tana.tana_auth.functions.routes.service.RouteAdminService;
import com.tana.tana_auth.functions.routes.service.RouteCompletionService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/routes")
public class RouteUserController {

    private final RouteAdminService routeAdminService;
    private final RouteCompletionService routeCompletionService;

    public RouteUserController(
        RouteAdminService routeAdminService,
        RouteCompletionService routeCompletionService
    ) {
        this.routeAdminService = routeAdminService;
        this.routeCompletionService = routeCompletionService;
    }

    @GetMapping
    public TanaApiResponse getRoutes() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRoutes())
            .build();
    }

    @GetMapping("/completed")
    public TanaApiResponse getCompletedRoutes() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeCompletionService.getCompletedRoutes())
            .build();
    }

    @PostMapping("/{routeId}/complete")
    public TanaApiResponse completeRoute(@PathVariable Long routeId) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeCompletionService.completeRoute(routeId))
            .build();
    }

    @GetMapping("/progress")
    public TanaApiResponse getRouteProgress() {
        return TanaApiResponse.builder().isSuccess(true)
            .resultData(routeCompletionService.getRouteProgress()).build();
    }

    @PostMapping("/{routeId}/restart")
    public TanaApiResponse restartRoute(@PathVariable Long routeId) throws TanaException {
        return TanaApiResponse.builder().isSuccess(true)
            .resultData(routeCompletionService.restartRoute(routeId)).build();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{routeId}/retake-stops/{placeId}")
    public TanaApiResponse updateRetakeStop(@PathVariable Long routeId, @PathVariable Long placeId,
        @org.springframework.web.bind.annotation.RequestParam boolean visited) throws TanaException {
        return TanaApiResponse.builder().isSuccess(true)
            .resultData(routeCompletionService.updateRetakeStop(routeId, placeId, visited)).build();
    }
}
