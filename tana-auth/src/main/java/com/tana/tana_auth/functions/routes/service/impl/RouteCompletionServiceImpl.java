package com.tana.tana_auth.functions.routes.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.routes.dto.CompletedRouteResponseDto;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import com.tana.tana_auth.functions.routes.repository.UserCompletedRouteRepository;
import com.tana.tana_auth.functions.routes.service.RouteCompletionService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.RouteMaster;
import com.tana.tana_common.model.UserCompletedRoute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class RouteCompletionServiceImpl implements RouteCompletionService {

    private final AuthConfig authConfig;
    private final AccountMasterRepository accountRepository;
    private final RouteRepository routeRepository;
    private final UserCompletedRouteRepository completedRouteRepository;

    public RouteCompletionServiceImpl(
        AuthConfig authConfig,
        AccountMasterRepository accountRepository,
        RouteRepository routeRepository,
        UserCompletedRouteRepository completedRouteRepository
    ) {
        this.authConfig = authConfig;
        this.accountRepository = accountRepository;
        this.routeRepository = routeRepository;
        this.completedRouteRepository = completedRouteRepository;
    }

    @Override
    public List<CompletedRouteResponseDto> getCompletedRoutes() {
        return getRouteProgress().stream().filter(item -> !item.isRetaking()).toList();
    }

    @Override
    public List<CompletedRouteResponseDto> getRouteProgress() {
        return completedRouteRepository
            .findAllByAccount_IdOrderByCompletedAtDesc(authConfig.getCurrentUserId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public CompletedRouteResponseDto completeRoute(Long routeId) throws TanaException {
        Long accountId = authConfig.getCurrentUserId();
        UserCompletedRoute completion = completedRouteRepository
            .findByAccount_IdAndRoute_RouteId(accountId, routeId)
            .orElseGet(UserCompletedRoute::new);

        if (completion.getUserCompletedRouteId() == null) {
            AccountMaster account = accountRepository.findById(accountId)
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
            RouteMaster route = routeRepository.findById(routeId)
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
            completion.setAccount(account);
            completion.setRoute(route);
            completion.setCompletedAt(LocalDateTime.now());
            completion = completedRouteRepository.save(completion);
        }

        if (isRetaking(completion)) {
            List<String> visited = completion.getRetakeVisitedPlaceIds() == null
                ? List.of() : completion.getRetakeVisitedPlaceIds();
            var required = completion.getRoute().getItinerary().stream()
                .filter(stop -> !String.join(" ",
                    Objects.toString(stop.getPriority(), ""), Objects.toString(stop.getTimeBlock(), ""))
                    .toLowerCase(java.util.Locale.ROOT).matches("(?s).*(bonus|optional|add-on|addon|detour).*"))
                .toList();
            if (required.isEmpty() || required.stream().anyMatch(stop -> !visited.contains(stop.getPlace().getId().toString()))) {
                throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
            }
            completion.setCompletedAt(LocalDateTime.now());
            completion = completedRouteRepository.save(completion);
        }
        return toResponse(completion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompletedRouteResponseDto restartRoute(Long routeId) throws TanaException {
        UserCompletedRoute completion = getCompletion(routeId);
        // Repeated requests must not erase progress in an already active retake.
        if (!isRetaking(completion)) {
            completion.setRestartedAt(LocalDateTime.now());
            completion.setRetakeVisitedPlaceIds(new ArrayList<>());
            completion = completedRouteRepository.save(completion);
        }
        return toResponse(completion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompletedRouteResponseDto updateRetakeStop(Long routeId, Long placeId, boolean visited) throws TanaException {
        UserCompletedRoute completion = getCompletion(routeId);
        if (!isRetaking(completion) || completion.getRoute().getItinerary().stream()
            .noneMatch(stop -> Objects.equals(stop.getPlace().getId(), placeId))) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }
        List<String> ids = new ArrayList<>(completion.getRetakeVisitedPlaceIds() == null
            ? List.of() : completion.getRetakeVisitedPlaceIds());
        ids.remove(placeId.toString());
        if (visited) ids.add(placeId.toString());
        completion.setRetakeVisitedPlaceIds(ids);
        return toResponse(completedRouteRepository.save(completion));
    }

    private UserCompletedRoute getCompletion(Long routeId) throws TanaException {
        return completedRouteRepository.findByAccount_IdAndRoute_RouteId(authConfig.getCurrentUserId(), routeId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
    }

    private boolean isRetaking(UserCompletedRoute completion) {
        return completion.getRestartedAt() != null
            && completion.getRestartedAt().isAfter(completion.getCompletedAt());
    }

    private CompletedRouteResponseDto toResponse(UserCompletedRoute completion) {
        return CompletedRouteResponseDto.builder()
            .routeId(completion.getRoute().getRouteId())
            .completedAt(completion.getCompletedAt())
            .restartedAt(completion.getRestartedAt())
            .retaking(isRetaking(completion))
            .visitedPlaceIds(completion.getRetakeVisitedPlaceIds() == null ? List.of() : completion.getRetakeVisitedPlaceIds())
            .build();
    }
}
