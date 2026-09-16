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

        return toResponse(completion);
    }

    private CompletedRouteResponseDto toResponse(UserCompletedRoute completion) {
        return CompletedRouteResponseDto.builder()
            .routeId(completion.getRoute().getRouteId())
            .completedAt(completion.getCompletedAt())
            .build();
    }
}
