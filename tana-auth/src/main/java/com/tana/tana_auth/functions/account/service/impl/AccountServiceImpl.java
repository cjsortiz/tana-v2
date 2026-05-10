package com.tana.tana_auth.functions.account.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.dto.*;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.login.repository.NotifyEmailRepository;
import com.tana.tana_auth.functions.account.service.AccountService;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.NotifyEmail;
import com.tana.tana_common.util.CommonUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AccountServiceImpl implements AccountService {

    @Value("${file.user-images}")
    private String uploadDir;

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    @Cacheable(
        value = "user-details",
        key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    public AccountResponseDto getUserProfileDetails() throws TanaException {
        Long accountId = authConfig.getCurrentUserId();

        AccountBasicDetailsDto basicDetailsDto = accountMasterRepository.getAccountBasic(accountId);

        if (ObjectUtils.isEmpty(basicDetailsDto)) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        Long explored = collectionRepository.getCollectionExploredCount(accountId);
        List<Long> completedCollections = collectionRepository.getCompletedCollections(accountId);

        return AccountResponseDto.builder()
            .userType(basicDetailsDto.getUserType())
            .accountId(basicDetailsDto.getAccountId())
            .displayName(formatName(basicDetailsDto.getFirstName()))
            .collectionExploredCount(explored)
            .badgeEarnedCount(completedCollections.size())
            .badgeResponseDtoList(getBadges(accountId))
            .userImage(basicDetailsDto.getUserImage())
            .spotVisitedCount(basicDetailsDto.getSpotVisitedCount())
            .firstName(basicDetailsDto.getFirstName())
            .lastName(basicDetailsDto.getLastName())
            .userLocation(basicDetailsDto.getUserLocation())
            .bio(basicDetailsDto.getBio())
            .build();
    }

    @Transactional
    @Override
    public AccountMasterResponseDto updateOnboardingStatus(AccountMasterRequestDto req) throws TanaException {
        AccountMaster user = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        if (!ObjectUtils.isEmpty(req.getIsOnboarded())) {
            user.setOnboarded(req.getIsOnboarded());
        }
        if (!ObjectUtils.isEmpty(req.getPreferMood())) {
            user.setPreferMood(req.getPreferMood());
        }
        user = accountMasterRepository.save(user);
        collectionService.clearCollectionDetailsCache(null, authConfig.getCurrentUserId());

        // Set updated account to security context
        authConfig.refreshUserDetails(user.getPreferMood(), user.isOnboarded());

        return AccountMasterResponseDto.builder()
            .isOnboarded(req.getIsOnboarded())
            .preferMood(req.getPreferMood())
            .build();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    @CacheEvict(
        value = "user-details",
        key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"
    )
    public AccountMasterResponseDto updateProfile(
        AccountMasterRequestDto dto, MultipartFile file
    ) throws TanaException {
        AccountMaster user = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserLocation(dto.getUserLocation());
        user.setBio(dto.getBio());
        user.setPreferLang(dto.getPreferLang());
        user.setUserType(Integer.valueOf(dto.getUserType()));

        if(!ObjectUtils.isEmpty(file)) {
            String imageString = commonUtils.uploadImage(user.getFirstName(),user.getId(),
                "users",uploadDir,file);

            user.setUserImage(imageString);
        }
        accountMasterRepository.save(user);

        collectionService.clearCollectionDetailsCache(null, authConfig.getCurrentUserId());

        return AccountMasterResponseDto.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .userLocation(dto.getUserLocation())
            .bio(dto.getBio())
            .preferLang(dto.getPreferLang())
            .build();
    }


    @Override
    @Caching(evict = {
        @CacheEvict(
            value = "user-details",
            key = "T(java.lang.String).valueOf(@authConfig.getCurrentUserId())"),

    })
    public void clearUserDetailsCache(String id) {
        System.out.println("Clear User Details cache : " + id);
    }

    private String formatName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]; // just "Maria"
        }

        String firstName = parts[0];
        String lastNameInitial = parts[parts.length - 1].substring(0, 1);

        return firstName + " " + lastNameInitial + ".";
    }

    public List<BadgeResponseDto> getBadges(Long accountId) {

        // spots grouped by collectionId
        Map<Long, List<BadgeSpotsDto>> spotMap = collectionRepository
            .getCollectionSpots(accountId)
            .stream()
            .collect(Collectors.groupingBy(
                SpotProgressProjection::getCollectionId,
                Collectors.mapping(
                    p -> new BadgeSpotsDto(p.getSpotName(), p.getIsVisited()),
                    Collectors.toList()
                )
            ));

        // attach spots to each badge
        return collectionRepository.getCollectionProgress(accountId)
            .stream()
            .map(p -> new BadgeResponseDto(
                p.getBadge(),
                p.getCollectionId(),
                p.getCollectionName(),
                p.getExplorerCount(),
                p.getExploredSpotsCount(),
                p.getTotalSpots(),
                p.getOverview(),
                p.getBadgeOverview(),
                spotMap.getOrDefault(p.getCollectionId(), List.of())
            ))
            .toList();
    }
}
