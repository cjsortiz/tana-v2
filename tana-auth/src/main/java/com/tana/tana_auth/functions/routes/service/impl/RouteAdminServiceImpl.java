package com.tana.tana_auth.functions.routes.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.routes.dto.RouteCategoryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteCategoryResponseDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteItineraryResponseDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerRequestDto;
import com.tana.tana_auth.functions.routes.dto.RoutePartnerResponseDto;
import com.tana.tana_auth.functions.routes.dto.RouteRequestDto;
import com.tana.tana_auth.functions.routes.dto.RouteResponseDto;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_auth.functions.routes.repository.RouteCategoryRepository;
import com.tana.tana_auth.functions.routes.repository.RouteItineraryRepository;
import com.tana.tana_auth.functions.routes.repository.RoutePartnerRepository;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import com.tana.tana_auth.functions.routes.service.RouteAdminService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.CollectionsMaster;
import com.tana.tana_common.model.PlaceMaster;
import com.tana.tana_common.model.RouteCategories;
import com.tana.tana_common.model.RouteItinerary;
import com.tana.tana_common.model.RouteMaster;
import com.tana.tana_common.model.RoutePartners;
import com.tana.tana_common.model.SpotVisited;
import com.tana.tana_common.util.CommonUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RouteAdminServiceImpl implements RouteAdminService {

    private final RouteCategoryRepository routeCategoryRepository;
    private final RoutePartnerRepository routePartnerRepository;
    private final RouteRepository routeRepository;
    private final RouteItineraryRepository routeItineraryRepository;
    private final PlacesRepository placesRepository;
    private final AuthConfig authConfig;
    private final SpotVisitedRepository spotVisitedRepository;
    private final CommonUtils commonUtils;

    public RouteAdminServiceImpl(
        RouteCategoryRepository routeCategoryRepository,
        RoutePartnerRepository routePartnerRepository,
        RouteRepository routeRepository,
        RouteItineraryRepository routeItineraryRepository,
        PlacesRepository placesRepository,
        AuthConfig authConfig,
        SpotVisitedRepository spotVisitedRepository,
        CommonUtils commonUtils
    ) {
        this.routeCategoryRepository = routeCategoryRepository;
        this.routePartnerRepository = routePartnerRepository;
        this.routeRepository = routeRepository;
        this.routeItineraryRepository = routeItineraryRepository;
        this.placesRepository = placesRepository;
        this.authConfig = authConfig;
        this.spotVisitedRepository = spotVisitedRepository;
        this.commonUtils = commonUtils;
    }

    @Override
    public List<RouteCategoryResponseDto> getRouteCategories() {
        return routeCategoryRepository.findAll()
            .stream()
            .map(this::toCategoryDto)
            .toList();
    }

    @Override
    public RouteCategoryResponseDto createRouteCategory(RouteCategoryRequestDto requestDto) throws TanaException {
        String categoryName = requireText(requestDto.getCategoryName());
        RouteCategories category = routeCategoryRepository.findByCategoryNameIgnoreCase(categoryName)
            .orElseGet(RouteCategories::new);
        category.setCategoryName(categoryName);
        category.setDescription(trimToNull(requestDto.getDescription()));

        return toCategoryDto(routeCategoryRepository.save(category));
    }

    @Override
    public List<RoutePartnerResponseDto> getRoutePartners() {
        return routePartnerRepository.findAllByOrderByRoutePartnerIdDesc()
            .stream()
            .map(this::toPartnerDto)
            .toList();
    }

    @Override
    public RoutePartnerResponseDto createRoutePartner(RoutePartnerRequestDto requestDto) throws TanaException {
        String partnerName = requireText(requestDto.getPartnerName());
        RoutePartners partner = routePartnerRepository.findByPartnerNameIgnoreCase(partnerName)
            .orElseGet(RoutePartners::new);
        partner.setPartnerName(partnerName);
        partner.setDescription(trimToNull(requestDto.getDescription()));
        partner.setLogoImage(trimToNull(requestDto.getLogoImage()));
        partner.setHelperText(trimToNull(requestDto.getHelperText()));
        partner.setActive(Optional.ofNullable(requestDto.getActive()).orElse(true));

        return toPartnerDto(routePartnerRepository.save(partner));
    }

    @Override
    public List<RouteResponseDto> getRoutes() {
        Map<Long, List<RouteItineraryResponseDto>> itineraryByRouteId =
            routeItineraryRepository.findAllByOrderByRouteRouteNameAscDayNumberAscStopOrderAsc()
                .stream()
                .map(this::toItineraryDto)
                .collect(Collectors.groupingBy(RouteItineraryResponseDto::getRouteId));

        return routeRepository.findAllByOrderByRouteIdDesc()
            .stream()
            .map(route -> toRouteDto(
                route,
                itineraryByRouteId.getOrDefault(route.getRouteId(), List.of())))
            .toList();
    }

    @Override
    public RouteResponseDto createRoute(RouteRequestDto requestDto) throws TanaException {
        RouteCategories category = routeCategoryRepository.findById(
            Optional.ofNullable(requestDto.getRouteCategoryId()).orElse(0L)
        ).orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        RoutePartners partner = Optional.ofNullable(requestDto.getRoutePartnerId())
            .map(routePartnerId -> routePartnerRepository.findById(routePartnerId)
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST)))
            .orElse(null);

        RouteMaster route = Optional.ofNullable(requestDto.getRouteId())
            .map(routeId -> routeRepository.findById(routeId)
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST)))
            .orElseGet(RouteMaster::new);
        route.setRouteName(requireText(requestDto.getRouteName()));
        route.setRouteCategory(category);
        route.setRoutePartner(partner);
        route.setRouteDuration(trimToNull(requestDto.getRouteDuration()));
        route.setOverview(trimToNull(requestDto.getOverview()));
        route.setHelperText(trimToNull(requestDto.getHelperText()));
        route.setRouteImage(trimToNull(requestDto.getRouteImage()));
        route.setTags(Optional.ofNullable(requestDto.getTags()).orElse(List.of())
            .stream()
            .map(String::trim)
            .filter(tag -> !tag.isBlank())
            .toList());

        return toRouteDto(routeRepository.save(route), List.of());
    }

    @Override
    public List<RouteItineraryResponseDto> getRouteItinerary() {
        return routeItineraryRepository.findAllByOrderByRouteRouteNameAscDayNumberAscStopOrderAsc()
            .stream()
            .map(this::toItineraryDto)
            .toList();
    }

    @Override
    public RouteItineraryResponseDto createRouteItinerary(RouteItineraryRequestDto requestDto) throws TanaException {
        RouteMaster route = routeRepository.findById(
            Optional.ofNullable(requestDto.getRouteId()).orElse(0L)
        ).orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        PlaceMaster place = placesRepository.findById(
            Optional.ofNullable(requestDto.getPlaceId()).orElse(0L)
        ).orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        RouteItinerary itinerary = new RouteItinerary();
        itinerary.setRoute(route);
        itinerary.setPlace(place);
        itinerary.setDayNumber(requirePositive(requestDto.getDayNumber()));
        itinerary.setStopOrder(requirePositive(requestDto.getStopOrder()));
        itinerary.setTimeBlock(trimToNull(requestDto.getTimeBlock()));
        itinerary.setPriority(trimToNull(requestDto.getPriority()));
        itinerary.setDetails(trimToNull(requestDto.getDetails()));

        return toItineraryDto(routeItineraryRepository.save(itinerary));
    }

    private RouteResponseDto toRouteDto(
        RouteMaster route,
        List<RouteItineraryResponseDto> itineraries
    ) {
        return RouteResponseDto.builder()
            .routeId(route.getRouteId())
            .routeName(route.getRouteName())
            .category(toCategoryDto(route.getRouteCategory()))
            .partner(toPartnerDto(route.getRoutePartner()))
            .routeDuration(route.getRouteDuration())
            .overview(route.getOverview())
            .helperText(route.getHelperText())
            .routeImage(route.getRouteImage())
            .tags(Optional.ofNullable(route.getTags()).orElse(List.of()))
            .itineraries(itineraries)
            .build();
    }

    private RoutePartnerResponseDto toPartnerDto(RoutePartners partner) {
        if (partner == null) {
            return null;
        }

        return RoutePartnerResponseDto.builder()
            .routePartnerId(partner.getRoutePartnerId())
            .partnerName(partner.getPartnerName())
            .description(partner.getDescription())
            .logoImage(partner.getLogoImage())
            .helperText(partner.getHelperText())
            .active(partner.getActive())
            .build();
    }

    private RouteCategoryResponseDto toCategoryDto(RouteCategories category) {
        return RouteCategoryResponseDto.builder()
            .routeCategoryId(category.getRouteCategoryId())
            .categoryName(category.getCategoryName())
            .description(category.getDescription())
            .build();
    }

    private RouteItineraryResponseDto toItineraryDto(RouteItinerary itinerary) {
        RouteMaster route = itinerary.getRoute();
        PlaceMaster place = itinerary.getPlace();
        Long currentUserId = authConfig.getCurrentUserId();
        SpotVisited spotVisited =
            spotVisitedRepository.findSpotsVisitedByPlaceAndAccountId(currentUserId, place.getId());
        boolean visited = spotVisited != null && Boolean.TRUE.equals(spotVisited.getIsVisited());
        List<String> spotImages = commonUtils.getSpotImages(place.getName());
        CollectionsMaster collection = Optional.ofNullable(place.getCollections())
            .orElse(List.of())
            .stream()
            .findFirst()
            .map(selection -> selection.getCollection())
            .orElse(null);

        return RouteItineraryResponseDto.builder()
            .routeItineraryId(itinerary.getRouteItineraryId())
            .routeId(route.getRouteId())
            .routeName(route.getRouteName())
            .routeCategoryName(route.getRouteCategory().getCategoryName())
            .placeId(place.getId())
            .placeName(place.getName())
            .dayNumber(itinerary.getDayNumber())
            .stopOrder(itinerary.getStopOrder())
            .timeBlock(itinerary.getTimeBlock())
            .priority(itinerary.getPriority())
            .details(itinerary.getDetails())
            .visited(visited)
            .spotImage(spotImages.isEmpty() ? null : spotImages.get(spotImages.size() - 1))
            .spotImages(spotImages)
            .collectionId(collection == null ? null : collection.getCollectionId())
            .collectionName(collection == null ? null : collection.getCollectionName())
            .badge(collection == null ? null : collection.getBadge())
            .build();
    }

    private String requireText(String value) throws TanaException {
        if (ObjectUtils.isEmpty(value) || value.trim().isBlank()) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return value.trim();
    }

    private Integer requirePositive(Integer value) throws TanaException {
        if (value == null || value <= 0) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return value;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }
}
