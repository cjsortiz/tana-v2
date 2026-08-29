package com.tana.tana_auth.functions.settings.repository;

import com.tana.tana_common.model.SettingsMaster;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<SettingsMaster, Long> {

    @Query(value = "SELECT * FROM SettingsMaster " +
        "WHERE settingsKey = :settingsKey", nativeQuery = true)
    Optional<SettingsMaster> findByKey(@Param("key") String settingsKey);
}
