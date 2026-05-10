package com.tana.tana_auth.functions.places.repository;

import com.tana.tana_auth.functions.places.dto.DashboardImageResponse;
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
    LEFT JOIN SpotVisited sv
        ON sv.placeId = pm.id
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

}
