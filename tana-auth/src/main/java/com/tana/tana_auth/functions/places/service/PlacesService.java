package com.tana.tana_auth.functions.places.service;

import com.tana.tana_auth.functions.places.dto.ReflectionRequestDto;
import com.tana.tana_auth.functions.places.dto.*;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PlacesService {

    PlacesRequestDto getAdminPlace(Long placeId) throws TanaException;

    void createPlaces(PlacesRequestDto placesRequestDto) throws TanaException;
    void createPlaces(PlacesRequestDto placesRequestDto, MultipartFile file) throws TanaException;

    PlacesDashboardResponseDto fetchDashboardImages(DashboardRequestDto requestDto);

    PlacesListResponseDto fetchAllPlaces(ExploreMapRequestDto requestDto);

    PlacesDetailsResponseDto fetchPlaceDetails(String id) throws TanaException;

    VisitedDto markVisited(VisitedDto requestDto) throws TanaException;

    void addReflection(String content, Long placeId, MultipartFile multipartFile) throws TanaException;

    void suggestSpot(
        String spotName,
        String category,
        String location,
        String vibe,
        MultipartFile[] photos
    ) throws TanaException, IOException;

    Page<ReflectionResponseDto> getReflections(ReflectionRequestDto requestDto);

    void saveSpotOrCollection(SaveRequestDto requestDto) throws TanaException;

    SavedCollectionResponseDto fetchSavedCollections() throws TanaException;

    Map<String, List<String>> fetchSpotImages(SpotImagesRequestDto requestDto);
}
