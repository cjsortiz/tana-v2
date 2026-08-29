package com.tana.tana_auth.functions.collections.repository;

import com.tana.tana_common.model.CuratorSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuratorSpotRepository extends JpaRepository<CuratorSpot, Long> {
    List<CuratorSpot> findAllByOrderByDisplayOrderAscCuratorSpotIdDesc();
    List<CuratorSpot> findAllByActiveTrueOrderByDisplayOrderAscCuratorSpotIdDesc();
    Optional<CuratorSpot> findByPlaceId(Long placeId);
}
