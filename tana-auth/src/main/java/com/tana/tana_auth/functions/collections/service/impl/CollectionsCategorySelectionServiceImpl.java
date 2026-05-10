package com.tana.tana_auth.functions.collections.service.impl;

import com.tana.tana_auth.functions.collections.dto.CollectionsListResponseDto;
import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import com.tana.tana_auth.functions.collections.repository.CollectionsCategorySelectionRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.collections.service.CollectionsCategorySelectionsService;
import com.tana.tana_common.constant.enums.MainCategoryTypeEnum;
import com.tana.tana_common.model.CollectionsCategorySelections;
import com.tana.tana_common.model.CollectionsMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionsCategorySelectionServiceImpl implements CollectionsCategorySelectionsService {

    @Autowired
    private CollectionsCategorySelectionRepository categorySelectionRepository;

    @Override
    @Cacheable("collections-category-selections")
    public CollectionsListResponseDto getCollectionCategorySelections() {
        System.out.println("Fetching from DB for collection selections...");
        List<CollectionsCategorySelections> collectionsMasters =
                categorySelectionRepository.findAll();


        if (ObjectUtils.isEmpty(collectionsMasters)) {
            return null;
        }


//        Map<String, List<CollectionsCategorySelections>> grouped =
//                collectionsMasters.stream()
//                        .collect(Collectors.groupingBy(
//                                cms ->
//                                        cms.getPlace().getSpot().getSpotId().toString()
//                        ));


        return CollectionsListResponseDto.builder()
//                .natureAndScenery(grouped.getOrDefault("1",List.of()))
//                .communityAndCulture(grouped.getOrDefault("1",))
                .build();

    }



}
