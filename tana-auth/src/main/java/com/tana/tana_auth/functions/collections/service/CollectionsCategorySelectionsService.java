package com.tana.tana_auth.functions.collections.service;

import com.tana.tana_auth.functions.collections.dto.CollectionsListResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface CollectionsCategorySelectionsService {

    CollectionsListResponseDto getCollectionCategorySelections();
}
