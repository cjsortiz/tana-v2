package com.tana.tana_auth.functions.collections.repository;

import com.tana.tana_auth.functions.account.dto.AccountResponseDto;
import com.tana.tana_auth.functions.account.dto.BadgeProjection;
import com.tana.tana_auth.functions.account.dto.BadgeResponseDto;
import com.tana.tana_auth.functions.account.dto.SpotProgressProjection;
import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import com.tana.tana_common.model.CollectionsMaster;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<CollectionsMaster, Long> {

    @Query(value =
            "SELECT " +
                "cm.collectionId, " +
                "cm.collectionName, " +
                "COUNT(cms.collectionSelectionId) as spotCounts, " +
                "cm.spot as category, " +
                "COUNT(DISTINCT sv.placeId) AS visitedCount, " +
                "cm.helperText, " +
                "cm.collectionImage " +
            "FROM CollectionsMaster cm " +
            "LEFT JOIN CollectionsCategorySelections cms " +
                "ON cms.collectionId = cm.collectionId " +
            "LEFT JOIN SpotVisited sv " +
                    "ON sv.placeId = cms.placeId " +
                    "AND sv.accountId = :accountId " +
            "GROUP BY cm.collectionId, cm.collectionName, cm.spot " +
            "HAVING COUNT(cms.collectionSelectionId) > 0",nativeQuery = true)
    List<CollectionsResponseDto> getCollectionsListResponse(@Param("accountId") Long accountId);

    @Query(value =
            "SELECT * " +
            "FROM CollectionsMaster " +
            "WHERE collectionId = :collectionId",nativeQuery = true)
    CollectionsMaster findByCollectionId(@Param("collectionId") Long collectionId);

    @Query(value =
            "SELECT " +
                "cm.collectionId, " +
                "cm.collectionName, " +
                "COUNT(cms.collectionSelectionId) as spotCounts, " +
                "cm.spot as category, " +
                "COUNT(DISTINCT sv.placeId) AS visitedCount," +
                "cm.helperText, " +
                "cm.collectionImage "  +
            "FROM CollectionsMaster cm " +
            "LEFT JOIN CollectionsCategorySelections cms " +
                "ON cms.collectionId = cm.collectionId " +
            "LEFT JOIN SpotVisited sv " +
                "ON sv.placeId = cms.placeId " +
                "AND sv.visitDate >= DATE_FORMAT(CURRENT_DATE(), '%Y-%m-01') " +
                "AND sv.visitDate < DATE_FORMAT(CURRENT_DATE() + INTERVAL 1 MONTH, '%Y-%m-01') " +
            "GROUP BY cm.collectionId " +
            "ORDER BY COUNT(sv.visitedId) DESC " +
            "LIMIT 1",
            nativeQuery = true)
    CollectionsResponseDto findFeaturedCollection();

    @Query(value = """
    SELECT
        cm.collectionId,
        cm.collectionName,
        COUNT(DISTINCT ccs.placeId) AS spotCounts,
        cm.spot AS category,
        COUNT(DISTINCT sv.placeId) AS visitedCount,
        cm.helperText,
        cm.collectionImage

    FROM CollectionsMaster cm

    INNER JOIN CollectionsCategorySelections ccs
        ON ccs.collectionId = cm.collectionId

    LEFT JOIN SpotVisited sv
        ON sv.placeId = ccs.placeId
        AND sv.accountId = :accountId
        AND sv.isVisited = true

    WHERE cm.moodType = :mood

    GROUP BY
        cm.collectionId,
        cm.collectionName,
        cm.spot,
        cm.helperText,
        cm.collectionImage,
        cm.moodPriority

    HAVING COUNT(DISTINCT sv.placeId)
        < COUNT(DISTINCT ccs.placeId)

    ORDER BY cm.moodPriority ASC

    LIMIT 1
    """, nativeQuery = true)
    CollectionsResponseDto findRecommendedCollection(
        @Param("mood") Integer mood,
        @Param("accountId") Long accountId
    );

    @Query(value = """
    SELECT
        c.badge,
        c.collectionId,
        c.collectionName,
        COALESCE((
            SELECT COUNT(*)
            FROM (
                SELECT sv2.accountId
                FROM SpotVisited sv2
                INNER JOIN CollectionsCategorySelections pcs2
                    ON sv2.placeId = pcs2.placeId
                WHERE pcs2.collectionId = c.collectionId
                  AND sv2.isVisited = 1
                GROUP BY sv2.accountId
                HAVING COUNT(DISTINCT sv2.placeId) = (
                    SELECT COUNT(*) FROM CollectionsCategorySelections
                    WHERE collectionId = c.collectionId
                )
            ) completers
        ), 0)                             AS explorerCount,
        COUNT(DISTINCT sv.visitedId)      AS exploredSpotsCount,
        COUNT(DISTINCT pcs.placeId)       AS totalSpots,
        c.overview,
        c.badgeOverview
    FROM CollectionsMaster c
    INNER JOIN CollectionsCategorySelections pcs ON pcs.collectionId = c.collectionId
    LEFT JOIN SpotVisited sv
        ON sv.placeId = pcs.placeId
        AND sv.accountId = :accountId
        AND sv.isVisited = 1
    GROUP BY c.collectionId, c.collectionName, c.overview, c.badgeOverview
    """, nativeQuery = true)
    List<BadgeProjection> getCollectionProgress(@Param("accountId") Long accountId);

    @Query(value = """
    SELECT
        pcs.collectionId                                                AS collectionId,
        pm.name                                                         AS spotName,
        CASE WHEN sv.visitedId IS NOT NULL THEN TRUE ELSE FALSE END     AS isVisited
    FROM CollectionsCategorySelections pcs
    INNER JOIN PlaceMaster pm
        ON pm.id = pcs.placeId
    LEFT JOIN SpotVisited sv
        ON sv.placeId  = pcs.placeId
        AND sv.accountId = :accountId
        AND sv.isVisited = 1
    ORDER BY pcs.collectionId, pm.name
    """, nativeQuery = true)
    List<SpotProgressProjection> getCollectionSpots(@Param("accountId") Long accountId);


    @Query("""
    SELECT COUNT(DISTINCT c.collectionId)
    FROM CollectionsMaster c
    JOIN c.collection pcs
    JOIN pcs.place p
    JOIN p.visitors sv
    WHERE sv.account.id = :accountId
      AND sv.isVisited = true
    """)
    Long getCollectionExploredCount(Long accountId);

    @Query(value = """
    SELECT COUNT(DISTINCT c.collectionId)
    FROM CollectionsMaster c
    JOIN CollectionsCategorySelections pcs 
        ON pcs.collectionId = c.collectionId
    LEFT JOIN SpotVisited sv
        ON sv.placeId = pcs.placeId
        AND sv.accountId = :accountId
        AND sv.isVisited = true
    GROUP BY c.collectionId
    HAVING COUNT(DISTINCT sv.visitedId) = COUNT(DISTINCT pcs.placeId)
    """, nativeQuery = true)
    List<Long> getCompletedCollections(Long accountId);


    @Query(value =
            "SELECT " +
                    "cm.collectionId, " +
                    "cm.collectionName, " +
                    "COUNT(cms.collectionSelectionId) as spotCounts, " +
                    "cm.spot as category, " +
                    "COUNT(DISTINCT sv.placeId) AS visitedCount, " +
                    "cm.helperText, " +
                    "cm.collectionImage " +
                    "FROM CollectionsMaster cm " +

                    // 🔥 ONLY saved collections
                    "INNER JOIN UserSaves us " +
                    "ON us.collectionId = cm.collectionId " +
                    "AND us.accountId = :accountId " +
                    "AND us.saved = true " +

                    "LEFT JOIN CollectionsCategorySelections cms " +
                    "ON cms.collectionId = cm.collectionId " +

                    "LEFT JOIN SpotVisited sv " +
                    "ON sv.placeId = cms.placeId " +
                    "AND sv.accountId = :accountId " +

                    "GROUP BY cm.collectionId, cm.collectionName, cm.spot " +
                    "HAVING COUNT(cms.collectionSelectionId) > 0",
            nativeQuery = true
    )
    List<CollectionsResponseDto> getSavedCollectionsListResponse(
            @Param("accountId") Long accountId
    );
}
