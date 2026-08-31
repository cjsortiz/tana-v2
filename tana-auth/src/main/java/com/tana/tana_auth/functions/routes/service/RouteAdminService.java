package com.tana.tana_auth.functions.routes.service;

import com.tana.tana_auth.functions.routes.dto.RouteCategoryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteCategoryResponseDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryResponseDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerRequestDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerResponseDto;
import com.tana.tana_auth.functions.routes.dto.RouteRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteResponseDto;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RouteAdminService {
    List<RouteCategoryResponseDto> getRouteCategories();
    RouteCategoryResponseDto createRouteCategory(RouteCategoryRequestDto requestDto) throws TanaException;
    List<RoutePartnerResponseDto> getRoutePartners();
    RoutePartnerResponseDto createRoutePartner(RoutePartnerRequestDto requestDto) throws TanaException;
    List<RouteResponseDto> getRoutes();
    RouteResponseDto createRoute(RouteRequestDto requestDto) throws TanaException;
    RouteResponseDto createRoute(RouteRequestDto requestDto, MultipartFile file) throws TanaException;
    List<RouteItineraryResponseDto> getRouteItinerary();
    RouteItineraryResponseDto createRouteItinerary(RouteItineraryRequestDto requestDto) throws TanaException;
}
