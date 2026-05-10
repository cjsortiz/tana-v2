package com.tana.tana_auth.functions.places.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.service.AccountService;
import com.tana.tana_auth.functions.collections.dto.CollectionsCategoryCustomQueryResponseDto;
import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.places.dto.ReflectionRequestDto;
import com.tana.tana_auth.functions.collections.repository.CollectionsCategorySelectionRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.places.dto.*;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.ReflectionRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_auth.functions.places.repository.UserSaveRepository;
import com.tana.tana_auth.functions.places.service.PlacesService;
import com.tana.tana_auth.functions.spot.repository.SpotRepository;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.MainCategoryTypeEnum;
import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import com.tana.tana_common.util.CommonUtils;
import com.tana.tana_common.util.MailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PlacesServiceImpl implements PlacesService {

    @Value("${file.upload-dir}")
    private String uploadDir;

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
    private AuthConfig authConfig;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MailService mailService;

    @Autowired
    private CommonUtils commonUtils;


    @Autowired
    private CollectionsCategorySelectionRepository collectionsCategorySelectionRepository;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void createPlaces(PlacesRequestDto requestDto) throws TanaException {
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

        if (!ObjectUtils.isEmpty(requestDto.getCollections())) {
            List<CollectionsMaster> collectionsMasters = collectionService.getAllCollections();
            Map<String, CollectionsMaster> collectionsMastersMap = collectionsMasters.stream()
                .collect(Collectors.toMap(CollectionsMaster::getCollectionName, c -> c));
            List<CollectionsCategorySelections> collectionsCategorySelections = new ArrayList<>();
            for (String collectionName : requestDto.getCollections()) {
                CollectionsCategorySelections selections = CollectionsCategorySelections.builder()
                    .place(place)
                    .collection(collectionsMastersMap.get(collectionName))
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
        key = "T(String).format('%s-%s-%s-%s-%s', " +
            "@authConfig.getCurrentUserId(), " +
            "#requestDto == null ? 'null' : #requestDto.placeId, " +
            "#requestDto == null ? 'null' : #requestDto.collectionId, " +
            "#requestDto == null ? 'null' : #requestDto.mainCategory, " +
            "#requestDto == null ? 'null' : #requestDto.search)"
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
            MainCategoryTypeEnum mainCategoryTypeEnum = MainCategoryTypeEnum.valueOf(requestDto.getMainCategory());
            placeMasters =
                repository.findPlacesByMainCategory(mainCategoryTypeEnum);
        } else if (!ObjectUtils.isEmpty(requestDto.getSearch())) {
            placeMasters = repository.searchPlaces(requestDto.getSearch());
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

        return PlacesListResponseDto.builder()
            .placeList(placesDetailsResponseDtoList)
            .build();
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

        String imageString = commonUtils.uploadImage(accountMaster.getFirstName(), reflection.getReflectionId(),
            placeMaster.getName(), uploadDir, multipartFile);

        // ✅ OPTIONAL: save image path in DB
        reflection.setImage(imageString);
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
                r.getAccount().getId().equals(authConfig.getCurrentUserId())
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

        return VisitedDto.builder()
            .placeId(spotVisited.getPlace().getId())
            .isVisited(spotVisited.getIsVisited())
            .build();
    }

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
                .findUserSave(user.getId(), requestDto.getCollectionId(), null)
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

    @Override
    public SavedCollectionResponseDto fetchSavedCollections() throws TanaException {
        List<CollectionsResponseDto> savedCollections =
            collectionRepository.getSavedCollectionsListResponse(authConfig.getCurrentUserId());

        return SavedCollectionResponseDto.builder()
            .collectionsResponseDtoList(savedCollections)
            .build();
    }


    private PlacesDetailsResponseDto buildPlaceDetailsResponseDto(PlaceMaster placeMaster) {
        final String[] locationList = Optional.ofNullable(placeMaster.getGpsLocation())
            .filter(s -> s.contains(","))
            .map(s -> s.split(","))
            .orElse(new String[]{"0", "0"});

        final String lat = locationList.length > 0 ? locationList[0] : "0";
        final String lng = locationList.length > 1 ? locationList[1] : "0";
        List<?> rawList = (List<?>) placeMaster.getSubCategoryTypeEnum();
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

        final List<CollectionsCategoryCustomQueryResponseDto> collectionsCategorySelectionsList =
            collectionsCategorySelectionRepository.findWithPlaceByCollectionId(
                authConfig.getCurrentUserId(), placeMaster.getCollections().get(0).getCollection().getCollectionId());

        final int index = IntStream.range(0, collectionsCategorySelectionsList.size())
            .filter(i -> collectionsCategorySelectionsList.get(i)
                .getPlace().getId()
                .equals(placeMaster.getId()))
            .findFirst()
            .orElse(-1);

        List<String> spotImages = getSpotImages(placeMaster.getName());

        CollectionsMaster cm = placeMaster.getCollections().get(0).getCollection();

        return PlacesDetailsResponseDto.builder()
            .id(placeMaster.getId())
            .collectionId(cm.getCollectionId())
            .collectionName(cm.getCollectionName())
            .index(index + 1)
            .name(placeMaster.getName())
            .overview(placeMaster.getOverview())
            .isVisited(visited)
            .subCategories(subCategories.subList(0, Math.min(2, subCategories.size())))
            .latitude(lat)
            .longitude(lng)
            .collectionImage(cm.getCollectionImage())
            .badge(cm.getBadge())
            .tanaTip(placeMaster.getTanaTip())
            .visitCount(placeMaster.getVisitors().stream()
                .filter(SpotVisited::getIsVisited).toList().size())
            .spotImages(spotImages)

            .build();
    }

    @Override
    public void suggestSpot(
        String spotName,
        String category,
        String location,
        String vibe,
        MultipartFile[] photos
    ) throws TanaException {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("to", "tanaBohol@gmail.com");
        metaData.put("category", category);
        metaData.put("submitterName", authConfig.getCurrentUser().getDisplayName());
        metaData.put("submitterEmail", authConfig.getCurrentUser().getEmailAddress());
        metaData.put("from", authConfig.getCurrentUser().getEmailAddress());
        metaData.put("spotName", spotName);
        metaData.put("location", location);
        metaData.put("vibe", vibe);

        if (!ObjectUtils.isEmpty(photos)) {
            metaData.put("photos", photos);
        }

        try {
            mailService.sendHtmlEmail(metaData);
        } catch (MessagingException e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        } catch (IOException e) {
            throw new RuntimeException("File handling failed", e);
        }
    }

    private List<String> getSpotImages(String spotName) {
        String sanitizedName = spotName.replaceAll("[^a-zA-Z0-9-_]", "");
        File spotDir = new File("D:\\tana-place-images\\" + sanitizedName);
        if (!spotDir.exists() || !spotDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = spotDir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
            .filter(File::isFile)
            .map(f -> sanitizedName + "/" + f.getName())
            .collect(Collectors.toList());
    }

}
