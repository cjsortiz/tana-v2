package com.tana.tana_auth.functions.collections.service;

import com.tana.tana_auth.functions.collections.dto.CollectionCreateRequestDto;
import com.tana.tana_auth.functions.collections.dto.CollectionAdminOptionDto;
import com.tana.tana_auth.functions.collections.dto.CollectionDetailsRequestDto;
import com.tana.tana_auth.functions.collections.dto.CollectionDetailsResponseDto;
import com.tana.tana_auth.functions.collections.dto.CollectionsListResponseDto;
import com.tana.tana_auth.functions.collections.dto.CuratorSpotRequestDto;
import com.tana.tana_auth.functions.collections.dto.CuratorSpotResponseDto;
import com.tana.tana_auth.functions.collections.dto.HomeV2ResponseDto;
import com.tana.tana_auth.functions.collections.dto.TanaStoryRequestDto;
import com.tana.tana_auth.functions.collections.dto.TanaStoryResponseDto;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.CollectionsMaster;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CollectionService {

    List<CollectionsMaster> getAllCollections();
    List<CollectionAdminOptionDto> getAdminCollectionOptions();
    CollectionAdminOptionDto createCollection(CollectionCreateRequestDto requestDto) throws TanaException;
    List<CuratorSpotResponseDto> getAdminCuratorSpots();
    CuratorSpotResponseDto saveCuratorSpot(CuratorSpotRequestDto requestDto) throws TanaException;
    List<TanaStoryResponseDto> getAdminTanaStories();
    TanaStoryResponseDto saveTanaStory(TanaStoryRequestDto requestDto) throws TanaException;
    CollectionsListResponseDto getCollectionsList();
    HomeV2ResponseDto getHomeV2();
    CollectionDetailsResponseDto getCollectionDetails(CollectionDetailsRequestDto requestDto) throws TanaException;
    void clearCollectionDetailsCache(Long collectionId, Long userId);
}
