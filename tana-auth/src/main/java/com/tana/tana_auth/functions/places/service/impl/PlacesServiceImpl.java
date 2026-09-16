package com.tana.tana_auth.functions.places.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.account.service.AccountService;
import com.tana.tana_auth.functions.collections.dto.CollectionsCategoryCustomQueryResponseDto;
import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.collections.repository.CollectionsCategorySelectionRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.places.dto.*;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.ReflectionRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_auth.functions.places.repository.UserSaveRepository;
import com.tana.tana_auth.functions.notification.repository.NotificationInboxRepository;
import com.tana.tana_auth.functions.places.service.PlacesService;
import com.tana.tana_auth.functions.spot.repository.SpotRepository;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.dto.EmailPhotoAttachment;
import com.tana.tana_common.constant.enums.MainCategoryTypeEnum;
import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import com.tana.tana_common.util.CommonUtils;
import com.tana.tana_common.util.MailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PlacesServiceImpl implements PlacesService {
    private static final int MAX_SUGGESTION_PHOTOS = 2;
    private static final long MAX_SUGGESTION_PHOTO_SIZE_BYTES = 5 * 1024 * 1024;
    private static final double EXPLORE_NEARBY_RADIUS_KM = 5.0;
    private static final double EARTH_RADIUS_KM = 6371.0088;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    @Autowired
    private PlacesRepository repository;

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private SpotVisitedRepository spotVisitedRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private ReflectionRepository reflectionRepository;

    @Autowired
    private UserSaveRepository userSaveRepository;

    @Autowired
    private NotificationInboxRepository notificationInboxRepository;

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MailService mailService;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private S3Client s3Client;


    @Autowired
    private CollectionsCategorySelectionRepository collectionsCategorySelectionRepository;

    @Caching(evict = {
        @CacheEvict(value = "place-list", allEntries = true),
        @CacheEvict(value = "collections", allEntries = true),
        @CacheEvict(value = "collections-list-response", allEntries = true),
        @CacheEvict(value = "home-v2-response", allEntries = true)
    })
    @Transactional(rollbackOn = Exception.class)
    @Override
    public void createPlaces(PlacesRequestDto requestDto) throws TanaException {
        createPlaces(requestDto, null);
    }

    @Override
    public void createPlaces(PlacesRequestDto requestDto, MultipartFile file) throws TanaException {
        PlaceMaster placeMaster = new PlaceMaster();
        placeMaster.setName(requestDto.getName());
        placeMaster.setTown(requestDto.getTown());
        placeMaster.setOverview(requestDto.getOverview());
        placeMaster.setTanaTip(requestDto.getTanaTip());


        if (!ObjectUtils.isEmpty(requestDto.getCategoryTypeEnum())) {
            placeMaster.setMainCategoryTypeEnum(
                MainCategoryTypeEnum.fromString(requestDto.getCategoryTypeEnum()));
        }

        if (!ObjectUtils.isEmpty(requestDto.getSubCategoryTypeEnum())) {
            List<SubCategoryTypeEnum> subCategoryTypeEnums =
                Optional.ofNullable(requestDto.getSubCategoryTypeEnum())
                    .orElse(List.of())
                    .stream()
                    .map(SubCategoryTypeEnum::fromString)
                    .toList();

            placeMaster.setSubCategoryTypeEnum(subCategoryTypeEnums);
        }

        placeMaster.setGoogleAddress(requestDto.getGoogleAddress());
        placeMaster.setIsTanaVerified(requestDto.getIsTanaVerified());
        placeMaster.setGpsLocation(requestDto.getGpsLocation());
        placeMaster.setOpeningDays(requestDto.getOpeningDays());
        placeMaster.setOpeningHours(requestDto.getOpeningHours());
        placeMaster.setFacebook(requestDto.getFacebook());
        placeMaster.setInstagram(requestDto.getInstagram());
        PlaceMaster place = repository.save(placeMaster);

        if (file != null && !file.isEmpty()) {
            String uploadedImage = commonUtils.uploadImage(
                "admin",
                place.getId(),
                place.getName(),
                file,
                "tana-place-images"
            );
            place.setImageStrings(List.of(uploadedImage));
            place = repository.save(place);
        }

        if (!ObjectUtils.isEmpty(requestDto.getCollections())) {
            List<CollectionsMaster> collectionsMasters = collectionService.getAllCollections();
            Map<String, CollectionsMaster> collectionsMastersMap = collectionsMasters.stream()
                .collect(Collectors.toMap(CollectionsMaster::getCollectionName, c -> c));
            List<CollectionsCategorySelections> collectionsCategorySelections = new ArrayList<>();
            for (String collectionName : requestDto.getCollections()) {
                CollectionsMaster collection = collectionsMastersMap.get(collectionName);
                if (collection == null) {
                    throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
                }
                CollectionsCategorySelections selections = CollectionsCategorySelections.builder()
                    .place(place)
                    .collection(collection)
                    .build();
                collectionsCategorySelections.add(selections);
            }
            collectionsCategorySelectionRepository.saveAll(collectionsCategorySelections);
        }
    }

    @Override
    public PlacesDashboardResponseDto fetchDashboardImages(DashboardRequestDto requestDto) {
        List<DashboardImageResponse> nearbyPlaces = repository.fetchNearby(requestDto.getLocation().getLatitude(),
            requestDto.getLocation().getLongitude());

        return PlacesDashboardResponseDto.builder()
            .nearby(nearbyPlaces)
            .build();

    }

    @Override
    @Cacheable(
        value = "place-list",
        key = "T(String).format('%s-%s-%s-%s-%s-%s-%s-%s-%s', " +
            "@authConfig.getCurrentUserId(), " +
            "#requestDto == null ? 'null' : #requestDto.placeId, " +
            "#requestDto == null ? 'null' : #requestDto.collectionId, " +
            "#requestDto == null ? 'null' : #requestDto.mainCategory, " +
            "#requestDto == null ? 'null' : #requestDto.search, " +
            "#requestDto == null ? 'null' : #requestDto.nearbyOnly, " +
            "#requestDto == null ? 'null' : #requestDto.savedOnly, " +
            "#requestDto == null ? 'null' : #requestDto.latitude, " +
            "#requestDto == null ? 'null' : #requestDto.longitude)"
    )
    public PlacesListResponseDto fetchAllPlaces(ExploreMapRequestDto requestDto) {
        if (ObjectUtils.isEmpty(requestDto)) {
            return null;
        }
        List<PlaceMaster> placeMasters = new ArrayList<>();
        if (!ObjectUtils.isEmpty(requestDto.getPlaceId())) {
            placeMasters =
                List.of(Objects.requireNonNull(
                    repository.findById(requestDto.getPlaceId())
                        .orElse(null)));
        } else if (!ObjectUtils.isEmpty(requestDto.getSearch())) {
            placeMasters = repository.searchPlaces(requestDto.getSearch());
        } else if (!ObjectUtils.isEmpty(requestDto.getCollectionId())) {
            placeMasters = repository.findPlacesByCollectionId(requestDto.getCollectionId());
        } else if (!ObjectUtils.isEmpty(requestDto.getMainCategory())) {
            placeMasters = repository.findPlacesByMainCategories(
                resolveMainCategoryFilter(requestDto.getMainCategory())
            );
        } else if (Boolean.TRUE.equals(requestDto.getSavedOnly())) {
            placeMasters = userSaveRepository.findSavedPlaces(authConfig.getCurrentUserId());
        } else {
            placeMasters = repository.findAll();
        }

        if (ObjectUtils.isEmpty(placeMasters)) {
            return PlacesListResponseDto.builder().build();
        }

        List<PlacesDetailsResponseDto> placesDetailsResponseDtoList =
            placeMasters.stream()
                .map(this::buildPlaceDetailsResponseDto
                ).toList();

        if (Boolean.TRUE.equals(requestDto.getNearbyOnly())) {
            if (requestDto.getLatitude() == null || requestDto.getLongitude() == null) {
                return PlacesListResponseDto.builder().placeList(List.of()).build();
            }

            placesDetailsResponseDtoList = placesDetailsResponseDtoList.stream()
                .peek(place -> place.setDistanceKm(distanceKm(
                    requestDto.getLatitude(), requestDto.getLongitude(),
                    parseCoordinate(place.getLatitude()), parseCoordinate(place.getLongitude()))))
                .filter(place -> place.getDistanceKm() <= EXPLORE_NEARBY_RADIUS_KM)
                .sorted(Comparator.comparingDouble(PlacesDetailsResponseDto::getDistanceKm))
                .toList();
        }

        return PlacesListResponseDto.builder()
            .placeList(placesDetailsResponseDtoList)
            .build();
    }

    private Double parseCoordinate(String value) {
        try {
            return value == null ? null : Double.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private double distanceKm(double latitude, double longitude, Double placeLatitude, Double placeLongitude) {
        if (placeLatitude == null || placeLongitude == null) {
            return Double.POSITIVE_INFINITY;
        }

        double latitudeDelta = Math.toRadians(placeLatitude - latitude);
        double longitudeDelta = Math.toRadians(placeLongitude - longitude);
        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
            + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(placeLatitude))
            * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<MainCategoryTypeEnum> resolveMainCategoryFilter(String mainCategory) {
        String normalized = Optional.ofNullable(mainCategory)
            .orElse("")
            .trim()
            .toLowerCase()
            .replace("&", "and")
            .replace("_", " ")
            .replace("-", " ");

        switch (normalized) {
            case "nature":
            case "scenery":
            case "nature and scenery":
                return List.of(MainCategoryTypeEnum.NATURE, MainCategoryTypeEnum.SCENERY);
            case "food":
            case "drink":
            case "food and drink":
                return List.of(MainCategoryTypeEnum.FOOD, MainCategoryTypeEnum.DRINK);
            case "community":
            case "culture":
            case "community and culture":
                return List.of(MainCategoryTypeEnum.COMMUNITY, MainCategoryTypeEnum.CULTURE);
            case "sports":
            case "sport":
            case "wellness":
            case "sports and wellness":
                return List.of(MainCategoryTypeEnum.SPORT, MainCategoryTypeEnum.WELLNESS);
            default:
                return List.of(MainCategoryTypeEnum.fromString(mainCategory));
        }
    }

    @Override
    public PlacesDetailsResponseDto fetchPlaceDetails(String id) throws TanaException {
        PlaceMaster placeMaster = repository.findById(Long.valueOf(id)).orElseThrow(
            () -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));


        return buildPlaceDetailsResponseDto(placeMaster);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addReflection(String content, Long placeId, MultipartFile multipartFile) throws TanaException {

        PlaceMaster placeMaster = repository.findById(placeId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        AccountMaster accountMaster = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));


        Reflection reflection = Reflection.builder()
            .account(accountMaster)
            .place(placeMaster)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build();

        reflectionRepository.save(reflection);

        if (!ObjectUtils.isEmpty(multipartFile)) {
            String imageString = commonUtils.uploadImage(accountMaster.getFirstName(), reflection.getReflectionId(),
                placeMaster.getName(), multipartFile, "tana-place-images");
            reflection.setImage(imageString);
        }

        reflectionRepository.save(reflection);
    }

    @Override
    public Page<ReflectionResponseDto> getReflections(ReflectionRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

        Page<Reflection> reflectionsPage =
            reflectionRepository.findByPlaceIdOrderByCreatedAtDesc(requestDto.getPlaceId(), pageable);

        return reflectionsPage.map(r -> {
            String lastNameInitial = "";

            if (r.getAccount().getLastName() != null && !r.getAccount().getLastName().isEmpty()) {
                lastNameInitial = r.getAccount().getLastName().charAt(0) + ".";
            }

            return new ReflectionResponseDto(
                r.getAccount().getFirstName() + " " + lastNameInitial,
                r.getCreatedAt(),
                r.getContent(),
                r.getAccount().getId().equals(authConfig.getCurrentUserId()),
                r.getImage()
            );
        });
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public VisitedDto markVisited(VisitedDto requestDto) throws TanaException {
        Long accountId = authConfig.getCurrentUserId();

        SpotVisited spotVisited =
            spotVisitedRepository.findSpotsVisitedByPlaceAndAccountId(accountId,
                requestDto.placeId);
        boolean wasNewlyVisited =
            Boolean.TRUE.equals(requestDto.isVisited)
                && (spotVisited == null || !Boolean.TRUE.equals(spotVisited.getIsVisited()));

        PlaceMaster placeMaster = repository.findById(requestDto.placeId).orElseThrow(
            () -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST)
        );

        AccountMaster accountMaster = accountMasterRepository.findById(accountId).orElseThrow(
            () -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST)
        );

        if (spotVisited == null) {
            // 👉 CREATE (insert)
            spotVisited = new SpotVisited();
            spotVisited.setPlace(placeMaster);
            spotVisited.setAccount(accountMaster);
            spotVisited.setIsVisited(requestDto.isVisited);
            spotVisited.setVisitDate(LocalDate.now());
        } else {
            // 👉 UPDATE
            spotVisited.setIsVisited(requestDto.isVisited); // or toggle if needed
        }

        spotVisitedRepository.save(spotVisited);
        // FORCE flush before eviction
        spotVisitedRepository.flush();
        // Clear collection details cache
        collectionService.clearCollectionDetailsCache(requestDto.collectionId, accountId);
        // Clear user details cache
        accountService.clearUserDetailsCache(accountId.toString());

        if (wasNewlyVisited) {
            createVisitNotifications(accountMaster, placeMaster, requestDto.collectionId);
        }

        return VisitedDto.builder()
            .placeId(spotVisited.getPlace().getId())
            .isVisited(spotVisited.getIsVisited())
            .build();
    }

    private void createVisitNotifications(
        AccountMaster account,
        PlaceMaster place,
        Long requestedCollectionId
    ) {
        Long collectionId = requestedCollectionId;
        CollectionsMaster collection = null;

        if (collectionId != null) {
            collection = collectionRepository.findByCollectionId(collectionId);
        } else if (!ObjectUtils.isEmpty(place.getCollections())) {
            collection = place.getCollections().get(0).getCollection();
            collectionId = collection.getCollectionId();
        }

        if (collectionId == null || collection == null) {
            Map<String, Object> visitData = new HashMap<>();
            visitData.put("spotId", place.getId());
            visitData.put("spotName", place.getName());
            addSpotImageData(visitData, place);

            notificationInboxRepository.save(NotificationInbox.builder()
                .account(account)
                .title(place.getName())
                .body("Visit logged. Keep exploring your collection.")
                .notificationType("SPOT_VISITED")
                .data(visitData)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
            return;
        }

        Long totalSpots = Optional.ofNullable(collectionRepository.countCollectionSpots(collectionId)).orElse(0L);
        Long visitedSpots = Optional.ofNullable(
            collectionRepository.countVisitedCollectionSpots(collectionId, account.getId())
        ).orElse(0L);
        Long remainingSpots = Math.max(totalSpots - visitedSpots, 0L);

        Map<String, Object> visitData = new HashMap<>();
        visitData.put("spotId", place.getId());
        visitData.put("spotName", place.getName());
        visitData.put("collectionId", collectionId);
        visitData.put("collectionName", collection.getCollectionName());
        visitData.put("collectionImage", collection.getCollectionImage());
        visitData.put("visitedSpots", visitedSpots);
        visitData.put("totalSpots", totalSpots);
        visitData.put("remainingSpots", remainingSpots);
        addSpotImageData(visitData, place);

        String progressBody =
            "You've visited " + visitedSpots + " of " + totalSpots + " spots. "
                + buildVisitProgressMessage(remainingSpots);

        notificationInboxRepository.save(NotificationInbox.builder()
            .account(account)
            .title(collection.getCollectionName() + "/" + place.getName())
            .body(progressBody)
            .notificationType("SPOT_VISITED")
            .data(visitData)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build());

        if (totalSpots > 0 && visitedSpots.equals(totalSpots)) {
            Map<String, Object> badgeData = new HashMap<>(visitData);
            badgeData.put("badge", collection.getBadge());
            badgeData.put("totalSpots", totalSpots);

            notificationInboxRepository.save(NotificationInbox.builder()
                .account(account)
                .title(collection.getBadge() != null ? collection.getBadge() : collection.getCollectionName())
                .body("You completed all " + totalSpots + " spots. Tap to celebrate.")
                .notificationType("BADGE_EARNED")
                .data(badgeData)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
        }
    }

    private String buildVisitProgressMessage(Long remainingSpots) {
        if (remainingSpots == null || remainingSpots <= 0) {
            return "You completed the badge.";
        }

        if (remainingSpots == 1) {
            return "One more and the badge is within reach.";
        }

        return remainingSpots + " more and the badge is within reach.";
    }

    private void addSpotImageData(Map<String, Object> data, PlaceMaster place) {
        List<String> spotImages = commonUtils.getSpotImages(place.getName());
        if (!ObjectUtils.isEmpty(spotImages)) {
            data.put("spotImage", spotImages.get(spotImages.size() - 1));
        }
    }

    @Caching(evict = {
        @CacheEvict(value = "saved-list", key = "@authConfig.getCurrentUserId()"),
        @CacheEvict(value = "place-list", allEntries = true)
    })
    @Transactional
    @Override
    public void saveSpotOrCollection(SaveRequestDto requestDto) throws TanaException {
        UserSaves saveSpotOrCollection = new UserSaves();
        AccountMaster user = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        if (!ObjectUtils.isEmpty(requestDto.getCollectionId())) {

            CollectionsMaster collectionsMaster =
                collectionRepository.findByCollectionId(requestDto.getCollectionId());

            if (ObjectUtils.isEmpty(collectionsMaster)) {
                throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
            }

            saveSpotOrCollection = userSaveRepository
                .findUserSave(user.getId(), requestDto.getCollectionId(), null)
                .orElseGet(() -> {
                    UserSaves newSave = new UserSaves();
                    newSave.setAccount(user);
                    newSave.setCollection(collectionsMaster);
                    return newSave;
                });

            saveSpotOrCollection.setSaved(requestDto.getIsSaving());
            collectionService.clearCollectionDetailsCache(collectionsMaster.getCollectionId(), user.getId());

        }

        if (!ObjectUtils.isEmpty(requestDto.getPlaceId())) {
            PlaceMaster placeMaster = repository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

            saveSpotOrCollection = userSaveRepository
                .findUserSave(user.getId(), null, requestDto.getPlaceId())
                .orElseGet(() -> {
                    UserSaves newSave = new UserSaves();
                    newSave.setAccount(user);
                    newSave.setPlace(placeMaster);
                    return newSave;
                });

            saveSpotOrCollection.setSaved(requestDto.getIsSaving());
        }

        userSaveRepository.save(saveSpotOrCollection);
        // Clear collection details cache
    }

    @Cacheable(
        value = "saved-list",
        key = "@authConfig.getCurrentUserId()"
    )
    @Override
    public SavedCollectionResponseDto fetchSavedCollections() throws TanaException {
        List<CollectionsResponseDto> savedCollections =
            collectionRepository.getSavedCollectionsListResponse(authConfig.getCurrentUserId());

        List<SavedSpotDetailsResponseDto> savedSpots =
            repository.getSavedSpotsListResponse(authConfig.getCurrentUserId());


        return SavedCollectionResponseDto.builder()
            .collectionsResponseDtoList(savedCollections)
            .placesResponseDtoList(savedSpots)
            .build();
    }

    @Override
    public Map<String, List<String>> fetchSpotImages(SpotImagesRequestDto requestDto) {
        return commonUtils.getSpotImagesBySpotNames(
            Optional.ofNullable(requestDto.getSpotNames()).orElse(List.of())
        );
    }


    private PlacesDetailsResponseDto buildPlaceDetailsResponseDto(PlaceMaster placeMaster) {
        final String[] locationList = Optional.ofNullable(placeMaster.getGpsLocation())
            .filter(s -> s.contains(","))
            .map(s -> s.split(","))
            .orElse(new String[]{"0", "0"});

        final String lat = locationList.length > 0 ? locationList[0] : "0";
        final String lng = locationList.length > 1 ? locationList[1] : "0";
        List<?> rawList = Optional.ofNullable((List<?>) placeMaster.getSubCategoryTypeEnum())
            .orElse(List.of());
        List<String> subCategories = rawList.stream()
            .map(val -> SubCategoryTypeEnum.valueOf(val.toString()).getSubTypeString())
            .toList();

        boolean visited = Optional.ofNullable(placeMaster.getVisitors())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(sv ->
                sv.getAccount().getId().equals(authConfig.getCurrentUserId())
                    && Boolean.TRUE.equals(sv.getIsVisited())
            );

        CollectionsMaster cm = Optional.ofNullable(placeMaster.getCollections())
            .orElse(List.of())
            .stream()
            .findFirst()
            .map(CollectionsCategorySelections::getCollection)
            .orElse(null);

        final List<CollectionsCategoryCustomQueryResponseDto> collectionsCategorySelectionsList =
            cm == null
                ? List.of()
                : collectionsCategorySelectionRepository.findWithPlaceByCollectionId(
                    authConfig.getCurrentUserId(), cm.getCollectionId());

        final int index = IntStream.range(0, collectionsCategorySelectionsList.size())
            .filter(i -> collectionsCategorySelectionsList.get(i)
                .getPlace().getId()
                .equals(placeMaster.getId()))
            .findFirst()
            .orElse(-1);

        List<String> spotImages = commonUtils.getSpotImages(placeMaster.getName());

        boolean isSaved = Optional.ofNullable(placeMaster.getUserSaves())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(sv ->
                sv.getAccount().getId().equals(authConfig.getCurrentUserId())
                    && Boolean.TRUE.equals(sv.getSaved())
            );

        return PlacesDetailsResponseDto.builder()
            .id(placeMaster.getId())
            .collectionId(cm == null ? null : cm.getCollectionId())
            .collectionName(cm == null ? null : cm.getCollectionName())
            .index(index + 1)
            .name(placeMaster.getName())
            .overview(placeMaster.getOverview())
            .isVisited(visited)
            .subCategories(subCategories.subList(0, Math.min(2, subCategories.size())))
            .latitude(lat)
            .longitude(lng)
            .collectionImage(cm == null ? null : cm.getCollectionImage())
            .badge(cm == null ? null : cm.getBadge())
            .mainCategory(placeMaster.getMainCategoryTypeEnum() == null
                ? null
                : placeMaster.getMainCategoryTypeEnum().getTypeString())
            .badgeDesc(cm == null ? null : cm.getBadgeOverview())
            .tanaTip(placeMaster.getTanaTip())
            .openingHours(placeMaster.getOpeningHours())
            .openingDays(placeMaster.getOpeningDays())
            .visitCount(Optional.ofNullable(placeMaster.getVisitors()).orElse(List.of()).stream()
                .filter(SpotVisited::getIsVisited).toList().size())
            .spotImages(spotImages)
            .saved(isSaved)
            .build();
    }

    @Override
    public void suggestSpot(
        String spotName,
        String category,
        String location,
        String vibe,
        MultipartFile[] photos
    ) throws TanaException, IOException {
        final String trimmedSpotName = requireSuggestionValue(spotName);
        final String trimmedCategory = requireSuggestionValue(category);
        final String trimmedLocation = requireSuggestionValue(location);
        final String trimmedVibe = Optional.ofNullable(vibe).map(String::trim).orElse("");
        final MultipartFile[] safePhotos = Optional.ofNullable(photos).orElse(new MultipartFile[0]);

        if (safePhotos.length > MAX_SUGGESTION_PHOTOS) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        Map<String, Object> metaData = new HashMap<>();
        metaData.put("to", "tanaBohol@gmail.com");
        metaData.put("category", trimmedCategory);
        metaData.put("submitterName", authConfig.getCurrentUser().getDisplayName());
        metaData.put("submitterEmail", authConfig.getCurrentUser().getEmailAddress());
        metaData.put("from", authConfig.getCurrentUser().getEmailAddress());
        metaData.put("spotName", trimmedSpotName);
        metaData.put("location", trimmedLocation);
        metaData.put("vibe", trimmedVibe);

        List<EmailPhotoAttachment> attachments = new ArrayList<>();
        for (MultipartFile photo : safePhotos) {
            if (photo == null || photo.isEmpty()) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }

            final String contentType = Optional.ofNullable(photo.getContentType()).orElse("");
            if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }

            if (photo.getSize() > MAX_SUGGESTION_PHOTO_SIZE_BYTES) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }

            attachments.add(
                new EmailPhotoAttachment(
                    sanitizeAttachmentFilename(photo.getOriginalFilename()),
                    contentType,
                    photo.getBytes()
                )
            );
        }
        metaData.put("photos", attachments);

        try {
            mailService.sendHtmlEmail(metaData);
        } catch (MessagingException e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        } catch (IOException e) {
            throw new RuntimeException("File handling failed", e);
        }
    }

    private String requireSuggestionValue(String value) throws TanaException {
        final String trimmed = Optional.ofNullable(value).map(String::trim).orElse("");

        if (trimmed.isBlank()) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return trimmed;
    }

    private String sanitizeAttachmentFilename(String fileName) {
        final String safeName = Optional.ofNullable(fileName)
            .map(String::trim)
            .filter(name -> !name.isBlank())
            .orElse("suggested-spot.jpg")
            .replaceAll("[\\\\/]", "_");

        return safeName.length() > 120 ? safeName.substring(safeName.length() - 120) : safeName;
    }
}
