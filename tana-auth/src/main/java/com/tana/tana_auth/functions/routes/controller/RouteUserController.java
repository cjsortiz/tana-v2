package com.tana.tana_auth.functions.routes.controller;

import com.tana.tana_auth.functions.routes.service.RouteAdminService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/routes")
public class RouteUserController {

    private final RouteAdminService routeAdminService;

    public RouteUserController(RouteAdminService routeAdminService) {
        this.routeAdminService = routeAdminService;
    }

    @GetMapping
    public TanaApiResponse getRoutes() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRoutes())
            .build();
    }
}
