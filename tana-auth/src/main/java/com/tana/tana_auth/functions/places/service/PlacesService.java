package com.tana.tana_auth.functions.places.service;

import com.tana.tana_auth.functions.places.dto.ReflectionRequestDto;
import com.tana.tana_auth.functions.places.dto.*;
import com.tana.tana_common.constant.exception.TanaException;
import org.apache.commons.math3.analysis.function.Tan;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface PlacesService {

    void createPlaces(PlacesRequestDto placesRequestDto) throws TanaException;

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
    ) throws TanaException;

     Page<ReflectionResponseDto> getReflections(ReflectionRequestDto requestDto);

     void saveSpotOrCollection(SaveRequestDto requestDto) throws TanaException;

     SavedCollectionResponseDto fetchSavedCollections() throws TanaException;
}
