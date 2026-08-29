package com.tana.tana_auth.functions.trip.repository;

import com.tana.tana_common.model.UserTripSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTripSettingsRepository extends JpaRepository<UserTripSettings, Long> {
    Optional<UserTripSettings> findByAccount_Id(Long accountId);
}
