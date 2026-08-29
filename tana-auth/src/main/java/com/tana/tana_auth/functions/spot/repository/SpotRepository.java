package com.tana.tana_auth.functions.spot.repository;

import com.tana.tana_common.model.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

    Spot findBySpotId(Long spotId);

    Optional<Spot> findBySpotNameIgnoreCase(String spotName);
}
