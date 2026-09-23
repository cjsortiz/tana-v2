package com.tana.tana_auth.functions.routes;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import com.tana.tana_auth.functions.routes.repository.UserCompletedRouteRepository;
import com.tana.tana_auth.functions.routes.service.impl.RouteCompletionServiceImpl;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteRetakeTest {
    private UserCompletedRouteRepository repository;
    private RouteCompletionServiceImpl service;
    private UserCompletedRoute completion;

    @BeforeEach
    void setup() {
        AuthConfig auth = mock(AuthConfig.class);
        when(auth.getCurrentUserId()).thenReturn(7L);
        repository = mock(UserCompletedRouteRepository.class);
        service = new RouteCompletionServiceImpl(auth, mock(AccountMasterRepository.class), mock(RouteRepository.class), repository);
        PlaceMaster place = new PlaceMaster();
        place.setId(12L);
        RouteItinerary stop = new RouteItinerary();
        stop.setPlace(place);
        RouteMaster route = new RouteMaster();
        route.setRouteId(3L);
        route.setItinerary(List.of(stop));
        completion = new UserCompletedRoute();
        completion.setUserCompletedRouteId(1L);
        completion.setRoute(route);
        completion.setCompletedAt(LocalDateTime.now().minusDays(1));
        when(repository.findByAccount_IdAndRoute_RouteId(7L, 3L)).thenReturn(Optional.of(completion));
        when(repository.findAllByAccount_IdOrderByCompletedAtDesc(7L)).thenReturn(List.of(completion));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void restartPreservesPreviousCompletionAndIsIdempotent() throws Exception {
        var previous = completion.getCompletedAt();
        assertTrue(service.restartRoute(3L).isRetaking());
        assertEquals(previous, completion.getCompletedAt());
        assertTrue(completion.getRetakeVisitedPlaceIds().isEmpty());
        service.updateRetakeStop(3L, 12L, true);
        var started = completion.getRestartedAt();
        service.restartRoute(3L);
        assertEquals(started, completion.getRestartedAt());
        assertEquals(List.of("12"), completion.getRetakeVisitedPlaceIds());
        assertTrue(service.getCompletedRoutes().isEmpty());
        assertEquals(1, service.getRouteProgress().size());
    }

    @Test
    void retakeCanCompleteAndRestartAgain() throws Exception {
        service.restartRoute(3L);
        assertThrows(TanaException.class, () -> service.completeRoute(3L));
        service.updateRetakeStop(3L, 12L, true);
        assertFalse(service.completeRoute(3L).isRetaking());
        assertEquals(1, service.getCompletedRoutes().size());
        assertTrue(service.restartRoute(3L).isRetaking());
        assertTrue(completion.getRetakeVisitedPlaceIds().isEmpty());
    }

    @Test
    void retakeChangesAreScopedToCurrentUserAndRouteStops() throws Exception {
        when(repository.findByAccount_IdAndRoute_RouteId(7L, 99L)).thenReturn(Optional.empty());
        assertThrows(TanaException.class, () -> service.restartRoute(99L));
        assertThrows(TanaException.class, () -> service.updateRetakeStop(3L, 12L, true));
        service.restartRoute(3L);
        assertThrows(TanaException.class, () -> service.updateRetakeStop(3L, 99L, true));
        service.updateRetakeStop(3L, 12L, true);
        service.updateRetakeStop(3L, 12L, true);
        assertEquals(List.of("12"), completion.getRetakeVisitedPlaceIds());
        service.updateRetakeStop(3L, 12L, false);
        assertTrue(completion.getRetakeVisitedPlaceIds().isEmpty());
    }
}
