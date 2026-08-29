package com.tana.tana_auth.functions.trip.repository;

import com.tana.tana_common.model.UserTripItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTripItemRepository extends JpaRepository<UserTripItem, Long> {
    List<UserTripItem> findAllByAccount_IdAndSavedTrue(Long accountId);
    Optional<UserTripItem> findByAccount_IdAndItemTypeAndItemId(Long accountId, String itemType, String itemId);
}
