package com.tana.tana_auth.functions.collections.repository;

import com.tana.tana_auth.functions.collections.dto.CollectionsCategoryCustomQueryResponseDto;
import com.tana.tana_common.model.CollectionsCategorySelections;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionsCategorySelectionRepository extends JpaRepository<CollectionsCategorySelections, Long> {

    @Query("""
    SELECT new com.tana.tana_auth.functions.collections.dto.CollectionsCategoryCustomQueryResponseDto(
        cs.collectionSelectionId,
        cs.collection,
        p
    )
    FROM CollectionsCategorySelections cs
    JOIN cs.place p
    LEFT JOIN SpotVisited sv
        ON sv.place.id = p.id
        AND sv.account.id = :accountId
    WHERE cs.collection.collectionId = :collectionId
    """)
    List<CollectionsCategoryCustomQueryResponseDto> findWithPlaceByCollectionId(
            @Param("accountId") Long accountId,
            @Param("collectionId") Long collectionId
    );
}
