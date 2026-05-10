package com.tana.tana_auth.functions.places.repository;

import com.tana.tana_common.model.SpotVisited;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotVisitedRepository extends JpaRepository<SpotVisited, Long> {

    @Query(value = "SELECT * " +
            "FROM SpotVisited " +
            "WHERE accountId = :accountId " +
            "AND placeId = :placeId",nativeQuery = true)
    SpotVisited findSpotsVisitedByPlaceAndAccountId(@Param("accountId") Long accountId,
                                                    @Param("placeId") Long placeId);


    @Query("""
    SELECT COUNT(DISTINCT sv.account.id)
    FROM SpotVisited sv
    WHERE sv.isVisited = true
      AND sv.place.id IN (
          SELECT cs.place.id
          FROM CollectionsCategorySelections cs
          WHERE cs.collection.collectionId = :collectionId
      )
    """)
    Long countVisitors(@Param("collectionId") Long collectionId);
}
