package com.tana.tana_auth.functions.places.repository;

import com.tana.tana_auth.functions.places.dto.DashboardImageResponse;
import com.tana.tana_auth.functions.places.dto.SavedSpotDetailsResponseDto;
import com.tana.tana_common.constant.enums.MainCategoryTypeEnum;
import com.tana.tana_common.model.PlaceMaster;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacesRepository extends JpaRepository<PlaceMaster, Long> {

    //    Add image in the response
    @Query(value = """
    SELECT 
        p.id AS id,
        p.name AS name,
        p.priceRange AS priceRange,
        p.isTanaVerified AS isTanaVerified,
        ST_Distance_Sphere(
            point(p.longitude, p.latitude),
            point(:lng, :lat)
        ) AS distance
    FROM PlaceMaster p
    WHERE ST_Distance_Sphere(
            point(p.longitude, p.latitude),
            point(:lng, :lat)
          ) <= 2000   -- distance in meters
    ORDER BY distance ASC
    LIMIT 20
    """, nativeQuery = true)
    List<DashboardImageResponse> fetchNearby(@Param("lat") Float lat, @Param("long") Float lng);

    @Query(value = """
    SELECT pm.*
    FROM PlaceMaster pm
    INNER JOIN SpotVisited sv
        ON sv.placeId = pm.id
        AND sv.isVisited = true
    GROUP BY pm.id
    ORDER BY COUNT(sv.placeId) DESC
    LIMIT 5
    """, nativeQuery = true)
    List<PlaceMaster> findTopFiveVisitedPlaces();

    @Query("""
    SELECT DISTINCT p
    FROM PlaceMaster p
    JOIN p.collections c
    WHERE c.collection.id = :collectionId
    """)
    List<PlaceMaster> findPlacesByCollectionId(@Param("collectionId") Long collectionId);

    @Query("""
    SELECT p
    FROM PlaceMaster p
    WHERE p.mainCategoryTypeEnum = :mainCategory
    """)
    List<PlaceMaster> findPlacesByMainCategory(@Param("mainCategory") MainCategoryTypeEnum mainCategory);

    @Query("""
    SELECT p
    FROM PlaceMaster p
    WHERE p.mainCategoryTypeEnum IN :mainCategories
    """)
    List<PlaceMaster> findPlacesByMainCategories(@Param("mainCategories") List<MainCategoryTypeEnum> mainCategories);


    @Query(value = """
    SELECT DISTINCT p.*
    FROM PlaceMaster p
    LEFT JOIN CollectionsCategorySelections ccs ON ccs.placeId = p.id
    LEFT JOIN CollectionsMaster cm ON cm.collectionId = ccs.collectionId
    WHERE
        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.town) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(cm.collectionName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """, nativeQuery = true)
    List<PlaceMaster> searchPlaces(@Param("keyword") String keyword);


    @Query(
        value =
            "SELECT " +
                "pm.id AS placeId, " +
                "pm.name as name, " +
                "cm.collectionName AS collectionName, " +
                "CASE WHEN sv.placeId IS NOT NULL THEN true ELSE false END AS isVisited, " +
                "NULL AS collectionImage " +
                "FROM PlaceMaster pm " +

                "INNER JOIN UserSaves us " +
                "ON us.placeId = pm.id " +
                "AND us.accountId = :accountId " +
                "AND us.saved = true " +

                "LEFT JOIN CollectionsCategorySelections cms " +
                "ON cms.placeId = pm.id " +

                "LEFT JOIN CollectionsMaster cm " +
                "ON cm.collectionId = cms.collectionId " +

                "LEFT JOIN SpotVisited sv " +
                "ON sv.placeId = pm.id " +
                "AND sv.accountId = :accountId " +

                "GROUP BY " +
                "pm.id, " +
                "cm.collectionName, " +
                "sv.placeId, " +
                "pm.imageStrings",
        nativeQuery = true
    )
    List<SavedSpotDetailsResponseDto> getSavedSpotsListResponse(
        @Param("accountId") Long accountId
    );
}
