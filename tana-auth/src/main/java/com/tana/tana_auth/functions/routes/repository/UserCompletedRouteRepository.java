package com.tana.tana_auth.functions.routes.repository;

import com.tana.tana_common.model.UserCompletedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompletedRouteRepository extends JpaRepository<UserCompletedRoute, Long> {
    List<UserCompletedRoute> findAllByAccount_IdOrderByCompletedAtDesc(Long accountId);
    Optional<UserCompletedRoute> findByAccount_IdAndRoute_RouteId(Long accountId, Long routeId);
}
