package com.tana.tana_auth.functions.collections.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.collections.dto.*;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.collections.repository.CollectionsCategorySelectionRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.places.dto.PlacesDetailsResponseDto;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {

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

    @Override
    @Cacheable("collections")
    public List<CollectionsMaster> getAllCollections() {
        System.out.println("Fetching from DB...");
        return collectionRepository.findAll();
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
        if(!authConfig.getCurrentUser().isOnboarded()) {
            featuredCollection = collectionRepository.findRecommendedCollection(
                Integer.valueOf(authConfig.getCurrentUser().getPreferMood()),
                authConfig.getCurrentUserId());
        } else {
            featuredCollection = null;
        }

        final List<SpotlightResponseDto> spotlightResponseDtoList =
                placesRepository.findTopFiveVisitedPlaces().stream()
                        .map(obj ->
                            SpotlightResponseDto.builder()
                                    .town(obj.getTown())
                                    .helperText(obj.getCollections().get(0).getCollection().getHelperText())
                                    .placeId(obj.getId())
                                    .mainCategory(obj.getMainCategoryTypeEnum().toString())
                                    .overview(obj.getOverview())
                                    .name(obj.getName())
                                    .collectionName(obj.getCollections().get(0).getCollection().getCollectionName())
                                    .collectionId(obj.getCollections().get(0).getCollection().getCollectionId())
                                    .build()
                        ).toList();

        final Map<String, List<CollectionsResponseDto>> grouped =
                collectionsDto.stream()
                        .collect(Collectors.groupingBy(
                                obj -> obj.getCategory().toString()
                        ));



        return CollectionsListResponseDto.builder()
                .natureAndScenery(grouped.getOrDefault("1",List.of()))
                .communityAndCulture(grouped.getOrDefault("2",List.of()))
                .foodAndDrink(grouped.getOrDefault("3",List.of()))
                .sportsAndWellness(grouped.getOrDefault("4",List.of()))
                .featuredCollection(featuredCollection)
                .spotlightResponseDtoList(spotlightResponseDtoList)
                .build();
    }

    @Cacheable(
            value = "collectionDetails",
            key = "#requestDto.collectionId + '_' + T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    @Override
    public CollectionDetailsResponseDto getCollectionDetails(
            CollectionDetailsRequestDto requestDto) throws TanaException{

        final CollectionsMaster collectionsMaster =
                collectionRepository.findByCollectionId(Long.parseLong(requestDto.getCollectionId()));

        if (ObjectUtils.isEmpty(collectionsMaster)) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        final Long userId = authConfig.getCurrentUserId();

        final List<CollectionsCategoryCustomQueryResponseDto> collectionsCategorySelectionsList =
                categorySelectionRepository.findWithPlaceByCollectionId(
                        userId,
                        Long.valueOf(requestDto.getCollectionId()));

        final Long explorerCounts = spotVisitedRepository.countVisitors(Long.valueOf(requestDto.getCollectionId()));

        if (ObjectUtils.isEmpty(collectionsCategorySelectionsList)){
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

                                    return PlacesDetailsResponseDto.builder()
                                            .id(obj.getPlace().getId())
                                            .name(obj.getPlace().getName())
                                            .overview(obj.getPlace().getOverview())
                                            .latitude(lat)
                                            .longitude(lng)
                                            .isVisited(visited)
                                            .subCategories(subCategories.subList(0, Math.min(2, subCategories.size())))
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
