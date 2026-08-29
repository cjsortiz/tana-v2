package com.tana.tana_auth.functions.routes.controller;

import com.tana.tana_auth.functions.routes.dto.RouteCategoryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteRequestDto;
import com.tana.tana_auth.functions.routes.service.RouteAdminService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/routes")
public class RouteAdminController {

    private final RouteAdminService routeAdminService;

    public RouteAdminController(RouteAdminService routeAdminService) {
        this.routeAdminService = routeAdminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/categories")
    public TanaApiResponse getRouteCategories() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRouteCategories())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/categories")
    public TanaApiResponse createRouteCategory(@RequestBody RouteCategoryRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRouteCategory(requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/partners")
    public TanaApiResponse getRoutePartners() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRoutePartners())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/partners")
    public TanaApiResponse createRoutePartner(@RequestBody RoutePartnerRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRoutePartner(requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin")
    public TanaApiResponse getRoutes() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRoutes())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin")
    public TanaApiResponse createRoute(@RequestBody RouteRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRoute(requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/itinerary")
    public TanaApiResponse getRouteItinerary() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.getRouteItinerary())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/itinerary")
    public TanaApiResponse createRouteItinerary(@RequestBody RouteItineraryRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRouteItinerary(requestDto))
            .build();
    }
}
