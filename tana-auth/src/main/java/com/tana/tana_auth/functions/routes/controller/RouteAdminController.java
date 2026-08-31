package com.tana.tana_auth.functions.routes.controller;

import com.tana.tana_auth.functions.routes.dto.RouteCategoryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteRequestDto;
import com.tana.tana_auth.functions.routes.service.RouteAdminService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/routes")
public class RouteAdminController {

    private final RouteAdminService routeAdminService;
    private final ObjectMapper objectMapper;

    public RouteAdminController(RouteAdminService routeAdminService, ObjectMapper objectMapper) {
        this.routeAdminService = routeAdminService;
        this.objectMapper = objectMapper;
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
    @PostMapping(value = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TanaApiResponse createRoute(@RequestBody RouteRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRoute(requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TanaApiResponse createRouteWithImage(
        @RequestPart("route") String routeJson,
        @RequestPart("file") MultipartFile file
    ) throws TanaException, JsonProcessingException {
        RouteRequestDto requestDto = objectMapper.readValue(routeJson, RouteRequestDto.class);

        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(routeAdminService.createRoute(requestDto, file))
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
