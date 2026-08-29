package com.tana.tana_auth.functions.routes.repository;

import com.tana.tana_common.model.RoutePartners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutePartnerRepository extends JpaRepository<RoutePartners, Long> {
    List<RoutePartners> findAllByOrderByRoutePartnerIdDesc();
    Optional<RoutePartners> findByPartnerNameIgnoreCase(String partnerName);
}
