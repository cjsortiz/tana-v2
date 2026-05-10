package com.tana.tana_auth.functions.collections.service;

import com.tana.tana_auth.functions.collections.dto.CollectionDetailsRequestDto;
import com.tana.tana_auth.functions.collections.dto.CollectionDetailsResponseDto;
import com.tana.tana_auth.functions.collections.dto.CollectionsListResponseDto;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.CollectionsMaster;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CollectionService {

    List<CollectionsMaster> getAllCollections();
    CollectionsListResponseDto getCollectionsList();
    CollectionDetailsResponseDto getCollectionDetails(CollectionDetailsRequestDto requestDto) throws TanaException;
    void clearCollectionDetailsCache(Long collectionId, Long userId);
}
