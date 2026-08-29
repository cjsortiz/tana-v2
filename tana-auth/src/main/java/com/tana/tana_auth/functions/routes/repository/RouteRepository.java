package com.tana.tana_auth.functions.routes.repository;

import com.tana.tana_common.model.RouteMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<RouteMaster, Long> {
    List<RouteMaster> findAllByOrderByRouteIdDesc();
}
