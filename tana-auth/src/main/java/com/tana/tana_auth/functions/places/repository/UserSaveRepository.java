package com.tana.tana_auth.functions.places.repository;

import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import com.tana.tana_common.model.UserSaves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSaveRepository extends JpaRepository<UserSaves, Long> {

    @Query("""
    SELECT us
    FROM UserSaves us
    WHERE us.account.id = :accountId
      AND (
            (:collectionId IS NOT NULL AND us.collection.id = :collectionId)
         OR (:placeId IS NOT NULL AND us.place.id = :placeId)
      )
    """)
    Optional<UserSaves> findUserSave(
            @Param("accountId") Long accountId,
            @Param("collectionId") Long collectionId,
            @Param("placeId") Long placeId
    );


}
