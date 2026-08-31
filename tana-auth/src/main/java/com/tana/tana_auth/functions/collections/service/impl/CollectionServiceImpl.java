package com.tana.tana_auth.functions.collections.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.collections.dto.*;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.collections.repository.CollectionsCategorySelectionRepository;
import com.tana.tana_auth.functions.collections.repository.CuratorSpotRepository;
import com.tana.tana_auth.functions.collections.repository.TanaStoryRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.places.dto.PlacesDetailsResponseDto;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_auth.functions.qr.model.QrType;
import com.tana.tana_auth.functions.qr.repository.QrScanEventRepository;
import com.tana.tana_auth.functions.qr.repository.QrTargetScanProjection;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import com.tana.tana_auth.functions.spot.repository.SpotRepository;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import com.tana.tana_common.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectionServiceImpl implements CollectionService {

    private static final List<String> HOME_COLLECTION_PREVIEW_ORDER = List.of(
        "Golden Hour",
        "Delicacy Trail",
        "Hidden Falls",
        "The Matcha Run",
        "Heritage Walks"
    );

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private PlacesRepository placesRepository;

    @Autowired
    private SpotVisitedRepository spotVisitedRepository;

    @Autowired
    private CollectionsCategorySelectionRepository categorySelectionRepository;

    @Autowired
    private CuratorSpotRepository curatorSpotRepository;

    @Autowired
    private TanaStoryRepository tanaStoryRepository;

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private QrScanEventRepository qrScanEventRepository;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    @Cacheable("collections")
    public List<CollectionsMaster> getAllCollections() {
        System.out.println("Fetching from DB...");
        return collectionRepository.findAll();
    }

    @Override
    public List<CollectionAdminOptionDto> getAdminCollectionOptions() {
        return collectionRepository.findAll()
            .stream()
            .map(this::toAdminOption)
            .toList();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "collections", allEntries = true),
        @CacheEvict(value = "collections-list-response", allEntries = true),
        @CacheEvict(value = "home-v2-response", allEntries = true)
    })
    public CollectionAdminOptionDto createCollection(CollectionCreateRequestDto requestDto) throws TanaException {
        String collectionName = requireText(requestDto.getCollectionName());
        String segment = requireText(requestDto.getSegment());
        Spot spot = spotRepository.findBySpotNameIgnoreCase(segment)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        CollectionsMaster collection = new CollectionsMaster();
        collection.setCollectionName(collectionName);
        collection.setOverview(trimToNull(requestDto.getOverview()));
        collection.setBadge(trimToNull(requestDto.getBadge()));
        collection.setBadgeOverview(trimToNull(requestDto.getBadgeOverview()));
        collection.setHelperText(trimToNull(requestDto.getHelperText()));
        collection.setCollectionImage(trimToNull(requestDto.getCollectionImage()));
        collection.setMoodType(requestDto.getMoodType());
        collection.setMoodPriority(requestDto.getMoodPriority());
        collection.setSpot(spot);

        return toAdminOption(collectionRepository.save(collection));
    }

    @Override
    public List<CuratorSpotResponseDto> getAdminCuratorSpots() {
        return curatorSpotRepository.findAllByOrderByDisplayOrderAscCuratorSpotIdDesc()
            .stream()
            .map(this::toCuratorSpotDto)
            .toList();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "collections-list-response", allEntries = true),
        @CacheEvict(value = "home-v2-response", allEntries = true)
    })
    public CuratorSpotResponseDto saveCuratorSpot(CuratorSpotRequestDto requestDto) throws TanaException {
        PlaceMaster place = placesRepository.findById(
            Optional.ofNullable(requestDto.getPlaceId()).orElse(0L)
        ).orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        CuratorSpot curatorSpot = curatorSpotRepository.findByPlaceId(place.getId())
            .orElseGet(CuratorSpot::new);
        curatorSpot.setPlace(place);
        curatorSpot.setDisplayOrder(Optional.ofNullable(requestDto.getDisplayOrder()).orElse(1));
        curatorSpot.setProofLabel(trimToNull(requestDto.getProofLabel()));
        curatorSpot.setActive(Optional.ofNullable(requestDto.getActive()).orElse(true));

        return toCuratorSpotDto(curatorSpotRepository.save(curatorSpot));
    }

    @Override
    public List<TanaStoryResponseDto> getAdminTanaStories() {
        return tanaStoryRepository.findAllByOrderByDisplayOrderAscTanaStoryIdDesc()
            .stream()
            .map(this::toTanaStoryDto)
            .toList();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "collections-list-response", allEntries = true),
        @CacheEvict(value = "home-v2-response", allEntries = true)
    })
    public TanaStoryResponseDto saveTanaStory(TanaStoryRequestDto requestDto) throws TanaException {
        return saveTanaStory(requestDto, null);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "collections-list-response", allEntries = true),
        @CacheEvict(value = "home-v2-response", allEntries = true)
    })
    public TanaStoryResponseDto saveTanaStory(
        TanaStoryRequestDto requestDto,
        MultipartFile file
    ) throws TanaException {
        TanaStory story = Optional.ofNullable(requestDto.getTanaStoryId())
            .flatMap(tanaStoryRepository::findById)
            .orElseGet(TanaStory::new);

        story.setTitle(requireText(requestDto.getTitle()));
        story.setLinkUrl(requireText(requestDto.getLinkUrl()));
        story.setTag(trimToNull(requestDto.getTag()));
        story.setCategory(trimToNull(requestDto.getCategory()));
        story.setDescription(trimToNull(requestDto.getDescription()));
        String previousImage = story.getImage();
        story.setImage(trimToNull(requestDto.getImage()));
        story.setDisplayOrder(Optional.ofNullable(requestDto.getDisplayOrder()).orElse(1));
        story.setActive(Optional.ofNullable(requestDto.getActive()).orElse(true));

        TanaStory savedStory = tanaStoryRepository.save(story);

        if (file != null && !file.isEmpty()) {
            String uploadedImage = commonUtils.uploadImage(
                "admin",
                savedStory.getTanaStoryId(),
                savedStory.getTitle(),
                file,
                "tana-story-images"
            );
            savedStory.setImage(uploadedImage);
            savedStory = tanaStoryRepository.save(savedStory);

            if (previousImage != null && !previousImage.equals(uploadedImage)) {
                commonUtils.deleteImage(previousImage);
            }
        }

        return toTanaStoryDto(savedStory);
    }

    private CollectionAdminOptionDto toAdminOption(CollectionsMaster collection) {
        return CollectionAdminOptionDto.builder()
            .collectionId(collection.getCollectionId())
            .collectionName(collection.getCollectionName())
            .segment(collection.getSpot() == null ? null : collection.getSpot().getSpotName())
            .build();
    }

    private String requireText(String value) throws TanaException {
        if (ObjectUtils.isEmpty(value) || value.trim().isBlank()) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    @Override
    @Cacheable(
        value = "collections-list-response",
        key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    public CollectionsListResponseDto getCollectionsList() {
        Long accountId = authConfig.getCurrentUserId();
        final List<CollectionsResponseDto> collectionsDto =
            collectionRepository.getCollectionsListResponse(accountId);

        CollectionsResponseDto featuredCollection = new CollectionsResponseDto();
        if (!authConfig.getCurrentUser().isOnboarded()) {
            featuredCollection = collectionRepository.findRecommendedCollection(
                Integer.valueOf(authConfig.getCurrentUser().getPreferMood()),
                authConfig.getCurrentUserId());
        } else {
            featuredCollection = null;
        }

        final List<SpotlightResponseDto> spotlightResponseDtoList =
            placesRepository.findTopFiveVisitedPlaces().stream()
                .map(obj -> toSpotlight(obj, "Popular right now"))
                .toList();
        final List<SpotlightResponseDto> curatorSpotResponseDtoList =
            curatorSpotRepository.findAllByActiveTrueOrderByDisplayOrderAscCuratorSpotIdDesc()
                .stream()
                .map(curatorSpot -> toSpotlight(
                    curatorSpot.getPlace(),
                    Optional.ofNullable(curatorSpot.getProofLabel()).orElse("Curator Pick")
                ))
                .toList();
        final List<TanaStoryResponseDto> tanaStoryResponseDtoList =
            tanaStoryRepository.findAllByActiveTrueOrderByDisplayOrderAscTanaStoryIdDesc()
                .stream()
                .map(this::toTanaStoryDto)
                .toList();

        final Map<String, List<CollectionsResponseDto>> grouped =
            collectionsDto.stream()
                .filter(obj -> obj.getCategory() != null)
                .collect(Collectors.groupingBy(
                    obj -> String.valueOf(obj.getCategory())
                ));


        return CollectionsListResponseDto.builder()
            .natureAndScenery(grouped.getOrDefault("1", List.of()))
            .communityAndCulture(grouped.getOrDefault("2", List.of()))
            .foodAndDrink(grouped.getOrDefault("3", List.of()))
            .sportsAndWellness(grouped.getOrDefault("4", List.of()))
            .featuredCollection(featuredCollection)
            .spotlightResponseDtoList(spotlightResponseDtoList)
            .curatorSpotResponseDtoList(curatorSpotResponseDtoList)
            .tanaStoryResponseDtoList(tanaStoryResponseDtoList)
            .build();
    }

    @Override
    @Cacheable(
        value = "home-v2-response",
        key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    public HomeV2ResponseDto getHomeV2() {
        final Long accountId = authConfig.getCurrentUserId();
        final Integer mood = Optional.ofNullable(authConfig.getCurrentUser().getPreferMood())
            .map(Integer::valueOf)
            .orElse(2);
        final List<CollectionsResponseDto> collectionPreview = buildHomeCollectionPreview(
            collectionRepository.getCollectionsListResponse(accountId)
        );
        final List<RouteResponseDto> routes = defaultRoutes();
        final List<RouteResponseDto> recommendedRoutes = routes.stream()
            .filter(route -> switch (mood) {
                case 1 -> route.getRouteId().equals("r3") || route.getRouteId().equals("r1");
                case 3 -> route.getRouteId().equals("r1") || route.getRouteId().equals("r3");
                case 4 -> route.getRouteId().equals("r4") || route.getRouteId().equals("r5");
                default -> route.getRouteId().equals("r2") || route.getRouteId().equals("r1");
            })
            .toList();

        final List<SpotlightResponseDto> topSpots = placesRepository.findTopFiveVisitedPlaces().stream()
            .map(obj -> toSpotlight(obj, "Most visited"))
            .toList();

        final List<RouteResponseDto> topRoutes = findTopVisitedRoutes();

        final List<SpotlightResponseDto> weeklySpots = placesRepository.findTopFiveVisitedPlaces().stream()
            .map(obj -> toSpotlight(obj, "Trending this week"))
            .toList();

        return HomeV2ResponseDto.builder()
            .trendingSpots(topSpots)
            .trendingRoutes(topRoutes)
            .weeklyTrendingSpots(weeklySpots)
            .collectionPreview(collectionPreview)
            .recommendedForYou(toMoodRecommendation(mood, recommendedRoutes))
            .panglaoAirportRoutes(routes.stream().filter(RouteResponseDto::getAirportPartner).toList())
            .routes(routes)
            .build();
    }

    private List<RouteResponseDto> findTopVisitedRoutes() {
        return qrScanEventRepository.findTargetAnalytics().stream()
            .filter(row -> row.getQrType() == QrType.ROUTE)
            .filter(row -> row.getTargetId() != null)
            .sorted(Comparator.comparing(QrTargetScanProjection::getScanCount).reversed())
            .limit(1)
            .map(row -> routeRepository.findById(row.getTargetId())
                .map(route -> toHomeRoute(route, row.getScanCount()))
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    private RouteResponseDto toHomeRoute(RouteMaster route, Long visitCount) {
        return RouteResponseDto.builder()
            .routeId(String.valueOf(route.getRouteId()))
            .name(route.getRouteName())
            .duration(route.getRouteDuration())
            .pace(route.getRouteDuration())
            .base(route.getRouteCategory() == null ? "" : route.getRouteCategory().getCategoryName())
            .mix("")
            .badge(route.getHelperText())
            .icon("🗺️")
            .blurb(route.getOverview())
            .description(route.getOverview())
            .airportPartner(Optional.ofNullable(route.getTags()).orElse(List.of()).stream()
                .anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains("bpia")))
            .tags(Optional.ofNullable(route.getTags()).orElse(List.of()))
            .stops(List.of())
            .visitCount(visitCount)
            .build();
    }

    private CuratorSpotResponseDto toCuratorSpotDto(CuratorSpot curatorSpot) {
        PlaceMaster place = curatorSpot.getPlace();

        return CuratorSpotResponseDto.builder()
            .curatorSpotId(curatorSpot.getCuratorSpotId())
            .placeId(place == null ? null : place.getId())
            .placeName(place == null ? "" : place.getName())
            .town(place == null ? "" : place.getTown())
            .displayOrder(curatorSpot.getDisplayOrder())
            .proofLabel(curatorSpot.getProofLabel())
            .active(curatorSpot.getActive())
            .build();
    }

    private TanaStoryResponseDto toTanaStoryDto(TanaStory story) {
        return TanaStoryResponseDto.builder()
            .tanaStoryId(story.getTanaStoryId())
            .title(story.getTitle())
            .tag(story.getTag())
            .category(story.getCategory())
            .description(story.getDescription())
            .image(story.getImage())
            .linkUrl(story.getLinkUrl())
            .displayOrder(story.getDisplayOrder())
            .active(story.getActive())
            .build();
    }

    private List<CollectionsResponseDto> buildHomeCollectionPreview(
        List<CollectionsResponseDto> collections
    ) {
        if (collections == null || collections.isEmpty()) {
            return List.of();
        }

        return collections.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(collection -> {
                int preferredIndex = HOME_COLLECTION_PREVIEW_ORDER.indexOf(
                    collection.getCollectionName()
                );
                return preferredIndex < 0
                    ? HOME_COLLECTION_PREVIEW_ORDER.size()
                    : preferredIndex;
            }))
            .limit(5)
            .toList();
    }

    private SpotlightResponseDto toSpotlight(PlaceMaster place, String proofLabel) {
        final List<String> spotImages = commonUtils.getSpotImages(place.getName());
        final CollectionsMaster collection = Optional.ofNullable(place.getCollections())
            .orElse(List.of())
            .stream()
            .findFirst()
            .map(CollectionsCategorySelections::getCollection)
            .orElse(null);

        return SpotlightResponseDto.builder()
            .town(place.getTown())
            .helperText(collection == null ? "" : collection.getHelperText())
            .placeId(place.getId())
            .mainCategory(place.getMainCategoryTypeEnum() == null ? "" : place.getMainCategoryTypeEnum().toString())
            .overview(place.getOverview())
            .name(place.getName())
            .collectionName(collection == null ? "" : collection.getCollectionName())
            .collectionId(collection == null ? null : collection.getCollectionId())
            .spotImage(spotImages.isEmpty() ? null : spotImages.get(spotImages.size() - 1))
            .proofLabel(proofLabel)
            .build();
    }

    private MoodRecommendationResponseDto toMoodRecommendation(Integer mood, List<RouteResponseDto> routes) {
        return switch (mood) {
            case 1 -> MoodRecommendationResponseDto.builder()
                .mood(1).emoji("🌿").name("Slow")
                .description("Unrushed routes matched to how you like to travel.")
                .tags(List.of("Panglao Beach Day", "Golden Hour Trail"))
                .routes(routes)
                .build();
            case 3 -> MoodRecommendationResponseDto.builder()
                .mood(3).emoji("🍳").name("Hungry")
                .description("Food first. The rest can wait.")
                .tags(List.of("Tagbilaran City Day", "Bohol Bee Farm"))
                .routes(routes)
                .build();
            case 4 -> MoodRecommendationResponseDto.builder()
                .mood(4).emoji("🗺️").name("Off the map")
                .description("The ones locals do not usually share.")
                .tags(List.of("Coastal and Farm", "North Bohol"))
                .routes(routes)
                .build();
            default -> MoodRecommendationResponseDto.builder()
                .mood(2).emoji("🧭").name("Curious")
                .description("A little of everything, thoughtfully chosen.")
                .tags(List.of("Countryside Tour", "Heritage Walks"))
                .routes(routes)
                .build();
        };
    }

    private List<RouteResponseDto> defaultRoutes() {
        return List.of(
            route("r1", "Tagbilaran City Day", "Half-day", "Tagbilaran City", "5 iconic : 3 Tana", "City Wanderer", "🏙️",
                "A landing-day or last-day intro, or a returning visitor's food-and-coffee crawl.",
                List.of(
                    stop("Morning", "Main", "Blood Compact Shrine", "Sandugo monument at Friendship Park.", true),
                    stop("Midday", "Main", "National Museum - Bohol", "Quiet heritage stop in the old Provincial Capitol.", true),
                    stop("Golden Hour", "Main", "Knights of Columbus Wharf", "Free waterfront sunset spot.", false),
                    stop("Nightcap", "Main", "Coclea Lounge Bar", "Final stop on the way back to Panglao.", true)
                )),
            route("r2", "The Countryside Tour, Upgraded", "Full day", "Tagbilaran to Carmen", "6 iconic : 3 Tana", "Countryside Classic", "🍫",
                "The exact circuit every van already sells, with Tana stops dropped into the gaps.",
                List.of(
                    stop("Morning", "Main", "Baclayon Church", "One of the oldest stone churches in the Philippines.", false),
                    stop("Lunch", "Main", "Loboc River Cruise", "Floating-restaurant buffet with live acoustic performers.", true),
                    stop("Afternoon", "Main", "Chocolate Hills", "Bohol's signature landmark.", true)
                )),
            route("r3", "Panglao Beach Day", "Half-full day", "Panglao / Dauis", "5 iconic : 3 Tana", "Island Bookend", "🏝️",
                "A zero-transit bookend day. The airport is on Panglao, and Tana fills the afternoon the boat leaves open.",
                List.of(
                    stop("Morning", "Main", "Balicasag or Virgin Island day trip", "The standard boat day from Panglao.", false),
                    stop("Midday", "Main", "Hinagdanan Cave", "Limestone cave with a swimmable underground pool.", false),
                    stop("Afternoon", "Main", "Alona Beach", "Panglao's most-visited beach strip.", true),
                    stop("Homebound", "Optional", "South Farm Panglao", "Organic farm walks and workshops.", false)
                )),
            route("r4", "Coastal and Farm", "2-3 days", "Jagna to Anda", "4 iconic : 3 Tana", "Far-Side Wanderer", "🌾",
                "The far side of the island most first-timers never reach.",
                List.of(
                    stop("Breakfast", "Main", "Painitan sa Baybay", "Roadside kakanin and sikwate in Jagna.", true),
                    stop("Morning", "Main", "Cadapdapan Rice Terraces", "Bohol's second Chocolate Hills.", true),
                    stop("Midday", "Main", "Can-umantad Falls", "Bohol's tallest waterfall.", false)
                )),
            route("r5", "North Bohol & Cabilao Island", "1-2 days", "Maribojoc to Tubigon", "4 iconic : 3 Tana", "Northern Diver", "🤿",
                "For divers and anyone connecting onward via Tubigon port to Cebu.",
                List.of(
                    stop("Morning", "Main", "Punta Cruz Watchtower", "Spanish-era watchtower with coastal views.", false),
                    stop("Overnight", "Main", "Cabilao Island", "Diving and marine-sanctuary island off Loon.", true),
                    stop("Lunch", "Main", "Atbang Farm", "Working cacao farm in Calape.", true)
                ))
        );
    }

    private RouteResponseDto route(String id, String name, String duration, String base, String mix, String badge, String icon, String blurb, List<RouteStopResponseDto> stops) {
        return RouteResponseDto.builder()
            .routeId(id)
            .name(name)
            .duration(duration)
            .pace(duration)
            .base(base)
            .mix(mix)
            .badge(badge)
            .icon(icon)
            .blurb(blurb)
            .description(blurb)
            .airportPartner(true)
            .tags(List.of("BPIA Route", "Curated", "Your pace"))
            .stops(stops)
            .build();
    }

    private RouteStopResponseDto stop(String when, String priority, String name, String description, Boolean verified) {
        return RouteStopResponseDto.builder()
            .when(when)
            .priority(priority)
            .name(name)
            .description(description)
            .verified(verified)
            .build();
    }

    @Cacheable(
        value = "collectionDetails",
        key = "#requestDto.collectionId + '_' + T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    @Override
    public CollectionDetailsResponseDto getCollectionDetails(
        CollectionDetailsRequestDto requestDto) throws TanaException {

        final String requestedCollectionId = Optional.ofNullable(requestDto.getCollectionId())
            .map(String::trim)
            .orElse("");
        final Long parsedCollectionId;
        try {
            parsedCollectionId = Long.parseLong(requestedCollectionId);
        } catch (NumberFormatException ex) {
            log.warn("Collection details requested with invalid collectionId='{}'", requestedCollectionId);
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        final Long userId = authConfig.getCurrentUserId();
        log.info("Collection details requested: collectionId={}, userId={}", parsedCollectionId, userId);

        final CollectionsMaster collectionsMaster =
            collectionRepository.findByCollectionId(parsedCollectionId);

        if (ObjectUtils.isEmpty(collectionsMaster)) {
            log.warn(
                "Collection details not found: requestedCollectionId={}, userId={}, existingCollectionIds={}",
                parsedCollectionId,
                userId,
                collectionRepository.findAll()
                    .stream()
                    .map(CollectionsMaster::getCollectionId)
                    .toList()
            );
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        final List<CollectionsCategoryCustomQueryResponseDto> collectionsCategorySelectionsList =
            categorySelectionRepository.findWithPlaceByCollectionId(
                userId,
                parsedCollectionId);

        final Long explorerCounts = spotVisitedRepository.countVisitors(parsedCollectionId);

        if (ObjectUtils.isEmpty(collectionsCategorySelectionsList)) {
            return CollectionDetailsResponseDto.builder()
                .collectionId(String.valueOf(collectionsMaster.getCollectionId()))
                .collectionName(collectionsMaster.getCollectionName())
                .overView(collectionsMaster.getOverview())
                .badge(collectionsMaster.getBadge())
                .build();
        }

        List<PlacesDetailsResponseDto> placesDetailsResponseDtoList =
            collectionsCategorySelectionsList.stream()
                .map(obj -> {
                        final String[] locationList = Optional.ofNullable(obj.getPlace().getGpsLocation())
                            .filter(s -> s.contains(","))
                            .map(s -> s.split(","))
                            .orElse(new String[]{"0", "0"});

                        final String lat = locationList.length > 0 ? locationList[0] : "0";
                        final String lng = locationList.length > 1 ? locationList[1] : "0";
                        List<?> rawList = (List<?>) obj.getPlace().getSubCategoryTypeEnum();

                        List<String> subCategories = rawList.stream()
                            .map(val -> SubCategoryTypeEnum.valueOf(val.toString()).getSubTypeString())
                            .toList();

                        boolean visited = Optional.ofNullable(obj.getPlace().getVisitors())
                            .orElse(Collections.emptyList())
                            .stream()
                            .anyMatch(sv ->
                                sv.getAccount().getId().equals(userId)
                                    && Boolean.TRUE.equals(sv.getIsVisited())
                            );

                    System.out.println(obj.getPlace().getOpeningDays());
                    System.out.println(obj.getPlace().getOpeningHours());

                    return PlacesDetailsResponseDto.builder()
                            .id(obj.getPlace().getId())
                            .name(obj.getPlace().getName())
                            .overview(obj.getPlace().getOverview())
                            .latitude(lat)
                            .longitude(lng)
                            .isVisited(visited)
                            .spotImages(commonUtils.getSpotImages(obj.getPlace().getName()))
                            .mainCategory(obj.getPlace().getMainCategoryTypeEnum().getTypeString())
                            .subCategories(subCategories.subList(0, Math.min(2, subCategories.size())))
                            .openingHours(obj.getPlace().getOpeningHours())
                            .openingDays(obj.getPlace().getOpeningDays())
                            .build();
                    }
                ).toList();

        System.out.println("CACHE KEY: " + requestDto.getCollectionId() + "_" + authConfig.getCurrentUserId());


        boolean isSaved = Optional.ofNullable(collectionsMaster.getUserSaves())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(sv ->
                sv.getAccount().getId().equals(userId)
                    && Boolean.TRUE.equals(sv.getSaved())
            );

        return CollectionDetailsResponseDto.builder()
            .saved(isSaved)
            .collectionId(String.valueOf(collectionsMaster.getCollectionId()))
            .collectionName(collectionsMaster.getCollectionName())
            .exploredCount(explorerCounts)
            .overView(collectionsMaster.getOverview())
            .badge(collectionsMaster.getBadge())
            .placeList(placesDetailsResponseDtoList)
            .helperText(collectionsMaster.getHelperText())
            .collectionImage(collectionsMaster.getCollectionImage())
            .build();
    }

    @Caching(evict = {
        @CacheEvict(
            value = "collectionDetails",
            key = "#collectionId + '_' + #userId"),
        @CacheEvict(
            value = "collections-list-response",
            key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"),
    })
    public void clearCollectionDetailsCache(Long collectionId, Long userId) {
        String key = collectionId + "_" + userId;
        System.out.println("Clear Collection Details cache : " + key);
    }
}
