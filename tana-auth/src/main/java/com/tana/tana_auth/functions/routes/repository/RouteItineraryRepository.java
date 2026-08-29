package com.tana.tana_auth.functions.routes.repository;

import com.tana.tana_common.model.RouteItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteItineraryRepository extends JpaRepository<RouteItinerary, Long> {
    List<RouteItinerary> findAllByOrderByRouteRouteNameAscDayNumberAscStopOrderAsc();
}
