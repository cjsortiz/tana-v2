package com.tana.tana_auth.functions.trip.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.trip.dto.TripDatesRequestDto;
import com.tana.tana_auth.functions.trip.dto.TripItemRequestDto;
import com.tana.tana_auth.functions.trip.dto.TripResponseDto;
import com.tana.tana_auth.functions.trip.repository.UserTripItemRepository;
import com.tana.tana_auth.functions.trip.repository.UserTripSettingsRepository;
import com.tana.tana_auth.functions.trip.service.TripService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.UserTripItem;
import com.tana.tana_common.model.UserTripSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripServiceImpl implements TripService {

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private UserTripSettingsRepository userTripSettingsRepository;

    @Autowired
    private UserTripItemRepository userTripItemRepository;

    @Override
    public TripResponseDto getTrip() throws TanaException {
        return buildResponse(authConfig.getCurrentUserId());
    }

    @Transactional
    @Override
    public TripResponseDto saveDates(TripDatesRequestDto requestDto) throws TanaException {
        Long accountId = authConfig.getCurrentUserId();
        AccountMaster account = findAccount(accountId);
        UserTripSettings settings = userTripSettingsRepository.findByAccount_Id(accountId)
            .orElseGet(UserTripSettings::new);

        settings.setAccount(account);
        settings.setStartDate(requestDto.getStartDate());
        settings.setEndDate(requestDto.getEndDate());
        settings.setDateLabel(requestDto.getDateLabel());
        userTripSettingsRepository.save(settings);

        return buildResponse(accountId);
    }

    @Transactional
    @Override
    public TripResponseDto saveItem(TripItemRequestDto requestDto) throws TanaException {
        Long accountId = authConfig.getCurrentUserId();
        AccountMaster account = findAccount(accountId);
        String itemType = normalizeItemType(requestDto.getItemType());
        String itemId = requestDto.getItemId() == null ? "" : requestDto.getItemId().trim();

        if (itemType.isBlank() || itemId.isBlank()) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        UserTripItem item = userTripItemRepository
            .findByAccount_IdAndItemTypeAndItemId(accountId, itemType, itemId)
            .orElseGet(UserTripItem::new);

        item.setAccount(account);
        item.setItemType(itemType);
        item.setItemId(itemId);
        item.setSaved(Boolean.TRUE.equals(requestDto.getSaved()));
        item.setGoing(Boolean.TRUE.equals(requestDto.getGoing()));
        userTripItemRepository.save(item);

        return buildResponse(accountId);
    }

    private TripResponseDto buildResponse(Long accountId) {
        UserTripSettings settings = userTripSettingsRepository.findByAccount_Id(accountId).orElse(null);
        List<UserTripItem> savedItems = userTripItemRepository.findAllByAccount_IdAndSavedTrue(accountId);

        return TripResponseDto.builder()
            .startDate(settings == null ? null : settings.getStartDate())
            .endDate(settings == null ? null : settings.getEndDate())
            .dateLabel(settings == null ? null : settings.getDateLabel())
            .savedRouteIds(savedItems.stream()
                .filter(item -> "ROUTE".equals(item.getItemType()))
                .map(UserTripItem::getItemId)
                .toList())
            .savedEventIds(savedItems.stream()
                .filter(item -> "EVENT".equals(item.getItemType()))
                .map(UserTripItem::getItemId)
                .toList())
            .goingEventIds(savedItems.stream()
                .filter(item -> "EVENT".equals(item.getItemType()) && Boolean.TRUE.equals(item.getGoing()))
                .map(UserTripItem::getItemId)
                .toList())
            .build();
    }

    private AccountMaster findAccount(Long accountId) throws TanaException {
        return accountMasterRepository.findById(accountId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
    }

    private String normalizeItemType(String itemType) {
        return itemType == null ? "" : itemType.trim().toUpperCase();
    }
}
