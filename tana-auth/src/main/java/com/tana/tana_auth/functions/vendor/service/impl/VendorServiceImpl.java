package com.tana.tana_auth.functions.vendor.service.impl;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.notification.repository.NotificationInboxRepository;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.repository.SpotVisitedRepository;
import com.tana.tana_auth.functions.vendor.dto.*;
import com.tana.tana_auth.functions.vendor.repository.VendorApplicationRepository;
import com.tana.tana_auth.functions.vendor.repository.VendorInviteRepository;
import com.tana.tana_auth.functions.vendor.repository.VendorPlaceOwnershipRepository;
import com.tana.tana_auth.functions.vendor.service.VendorService;
import com.tana.tana_common.constant.EncryptionProperties;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.AccessLevel;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import com.tana.tana_common.util.CommonUtils;
import com.tana.tana_common.util.password.PasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d");

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private PlacesRepository placesRepository;

    @Autowired
    private SpotVisitedRepository spotVisitedRepository;

    @Autowired
    private NotificationInboxRepository notificationInboxRepository;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private VendorPlaceOwnershipRepository vendorPlaceOwnershipRepository;

    @Autowired
    private VendorApplicationRepository vendorApplicationRepository;

    @Autowired
    private VendorInviteRepository vendorInviteRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Override
    @Transactional
    public AdminDashboardDto getAdminDashboard() {
        List<PlaceMaster> spots = placesRepository.findAll();
        return AdminDashboardDto.builder()
            .totalSpots(spots.size())
            .totalVendors((int) vendorPlaceOwnershipRepository.findAllActiveOwnerships()
                .stream()
                .map(ownership -> ownership.getVendorAccount().getId())
                .distinct()
                .count())
            .pendingApplications(vendorApplicationRepository.countByStatus("pending").intValue())
            .totalStamps(spots.stream().mapToInt(this::visitCount).sum())
            .build();
    }

    @Override
    @Transactional
    public List<AdminVendorDto> getAdminVendors() {
        return vendorPlaceOwnershipRepository.findAllActiveOwnerships()
            .stream()
            .map(this::toAdminVendorDto)
            .toList();
    }

    @Override
    @Transactional
    public List<VendorSpotDto> getAdminSpotAnalytics() {
        return placesRepository.findAll()
            .stream()
            .map(this::toSpotDto)
            .toList();
    }

    @Override
    @Transactional
    public List<VendorApplicationResponseDto> getAdminApplications() {
        return vendorApplicationRepository.findAllByOrderByApplicationIdDesc()
            .stream()
            .map(this::toApplicationDto)
            .toList();
    }

    @Override
    @Transactional
    public VendorApplicationResponseDto submitApplication(VendorApplicationRequestDto requestDto) throws TanaException {
        VendorApplication application = VendorApplication.builder()
            .spotName(requireText(requestDto.getSpotName()))
            .ownerName(requireText(requestDto.getOwnerName()))
            .phone(requireText(requestDto.getPhone()))
            .email(requireText(requestDto.getEmail()).toLowerCase(Locale.ROOT))
            .category(requireText(requestDto.getCategory()))
            .location(requireText(requestDto.getLocation()))
            .worthVisiting(requireText(requestDto.getWorthVisiting()))
            .operatingHours(requireText(requestDto.getOperatingHours()))
            .habalHabalPickup(requireText(requestDto.getHabalHabalPickup()))
            .commuteRoute(requestDto.getCommuteRoute())
            .walkTrekInfo(requestDto.getWalkTrekInfo())
            .status("pending")
            .build();

        return toApplicationDto(vendorApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public List<VendorApplicationResponseDto> getPendingApplications() {
        return vendorApplicationRepository.findByStatusOrderByApplicationIdDesc("pending")
            .stream()
            .map(this::toApplicationDto)
            .toList();
    }

    @Override
    @Transactional
    public VendorInviteResponseDto approveApplication(
        Long applicationId,
        VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        VendorApplication application = vendorApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        PlaceMaster place = placesRepository.findById(requestDto.getPlaceId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        application.setStatus("approved");
        application.setLinkedPlace(place);
        application.setReviewNote(requestDto.getReviewNote());
        vendorApplicationRepository.save(application);

        VendorInvite invite = VendorInvite.builder()
            .token(UUID.randomUUID().toString().replace("-", ""))
            .email(application.getEmail())
            .displayName(application.getOwnerName())
            .place(place)
            .application(application)
            .expiresAt(LocalDateTime.now().plusDays(30))
            .agreementAccepted(false)
            .status("pending")
            .build();

        return toInviteDto(vendorInviteRepository.save(invite));
    }

    @Override
    @Transactional
    public VendorApplicationResponseDto followUpApplication(
        Long applicationId,
        VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        VendorApplication application = vendorApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        application.setStatus("follow_up");
        application.setReviewNote(safe(requestDto.getReviewNote(), "Needs follow-up from Tana."));
        return toApplicationDto(vendorApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public VendorApplicationResponseDto declineApplication(
        Long applicationId,
        VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        VendorApplication application = vendorApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        application.setStatus("declined");
        application.setReviewNote(safe(
            requestDto.getReviewNote(),
            "Not adding in this category right now."
        ));
        return toApplicationDto(vendorApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public VendorInviteResponseDto getInvite(String token) throws TanaException {
        return toInviteDto(requireInvite(token));
    }

    @Override
    @Transactional
    public VendorInviteResponseDto acceptInvite(String token, VendorInviteAcceptRequestDto requestDto) throws TanaException {
        VendorInvite invite = requireInvite(token);
        if (isInviteExpired(invite) || !"pending".equalsIgnoreCase(invite.getStatus())) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }
        if (!Boolean.TRUE.equals(requestDto.getAgreementAccepted())) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
        if (accountMasterRepository.findByEmail(invite.getEmail()) != null) {
            throw new TanaException(CustomCodeErrors.EMAIL_ALR_EXIST);
        }

        AccountMaster account = new AccountMaster();
        account.setUserName(createVendorUsername(invite.getEmail()));
        account.setEmail(invite.getEmail());
        account.setFirstName(requireText(requestDto.getDisplayName()));
        try {
            account.setPassword(passwordUtil.encrypt(
                requireText(requestDto.getPassword()),
                encryptionProperties.getKeyConfig(),
                encryptionProperties.getSalt()
            ));
        } catch (Exception exception) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
        account.setPreferLang("en");
        account.setPreferMood("1");
        account.setUserType(2);
        account.setActive(true);
        account.setAccessLevel(AccessLevel.VENDOR);
        AccountMaster savedAccount = accountMasterRepository.save(account);

        VendorPlaceOwnership ownership = VendorPlaceOwnership.builder()
            .vendorAccount(savedAccount)
            .place(invite.getPlace())
            .role("owner")
            .status("active")
            .build();
        vendorPlaceOwnershipRepository.save(ownership);

        invite.setAcceptedAccount(savedAccount);
        invite.setAgreementAccepted(true);
        invite.setAcceptedAt(LocalDateTime.now());
        invite.setStatus("accepted");

        return toInviteDto(vendorInviteRepository.save(invite));
    }

    @Override
    @Transactional
    public List<VendorOwnershipDto> getOwnerships() {
        return vendorPlaceOwnershipRepository
            .findActiveByVendorAccountId(authConfig.getCurrentUserId())
            .stream()
            .map(this::toOwnershipDto)
            .toList();
    }

    @Override
    @Transactional
    public VendorOwnershipDto linkPlaceOwnership(VendorPlaceOwnershipRequestDto requestDto) throws TanaException {
        Long vendorAccountId = requestDto.getVendorAccountId() == null
            ? authConfig.getCurrentUserId()
            : requestDto.getVendorAccountId();
        AccountMaster vendor = accountMasterRepository.findById(vendorAccountId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        PlaceMaster place = placesRepository.findById(requestDto.getPlaceId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        if (vendor.getAccessLevel() == AccessLevel.USER) {
            vendor.setAccessLevel(AccessLevel.VENDOR);
            accountMasterRepository.save(vendor);
        }

        VendorPlaceOwnership ownership = vendorPlaceOwnershipRepository
            .findActiveOwnership(vendorAccountId, place.getId())
            .orElseGet(VendorPlaceOwnership::new);
        ownership.setVendorAccount(vendor);
        ownership.setPlace(place);
        ownership.setRole(safe(requestDto.getRole(), "owner"));
        ownership.setStatus(safe(requestDto.getStatus(), "active"));

        return toOwnershipDto(vendorPlaceOwnershipRepository.save(ownership));
    }

    @Override
    @Transactional
    public VendorDetailsDto getDetails() throws TanaException {
        AccountMaster account = currentAccount();
        List<PlaceMaster> spots = vendorPlaces();
        int visits = spots.stream().mapToInt(this::visitCount).sum();
        int reflections = spots.stream().mapToInt(this::reflectionCount).sum();
        PlaceMaster leadSpot = spots.stream().findFirst().orElse(null);

        return VendorDetailsDto.builder()
            .vendorId(account.getId())
            .businessName(leadSpot != null ? leadSpot.getName() : displayName(account))
            .ownerName(displayName(account))
            .category(leadSpot != null ? categoryLabel(leadSpot) : "Partner")
            .status("active")
            .location(leadSpot != null ? safe(leadSpot.getTown(), leadSpot.getGoogleAddress()) : safe(account.getUserLocation(), "Bohol"))
            .contactEmail(account.getEmail())
            .contactNumber(null)
            .rating(null)
            .totalSpots(spots.size())
            .totalVisits(visits)
            .totalReflections(reflections)
            .coverImage(firstSpotImage(leadSpot))
            .build();
    }

    @Override
    @Transactional
    public List<VendorSpotDto> getSpots() {
        return vendorPlaces().stream().map(this::toSpotDto).toList();
    }

    @Override
    @Transactional
    public VendorAnalyticsDto getAnalytics() {
        List<PlaceMaster> spots = vendorPlaces();
        List<SpotVisited> visits = spots.stream()
            .flatMap(spot -> Optional.ofNullable(spot.getVisitors()).orElse(List.of()).stream())
            .filter(visit -> Boolean.TRUE.equals(visit.getIsVisited()))
            .toList();
        List<Reflection> reflections = spots.stream()
            .flatMap(spot -> Optional.ofNullable(spot.getReflections()).orElse(List.of()).stream())
            .sorted(Comparator.comparing(Reflection::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        Map<LocalDate, Long> visitsByDay = visits.stream()
            .filter(visit -> visit.getVisitDate() != null)
            .collect(Collectors.groupingBy(SpotVisited::getVisitDate, Collectors.counting()));
        LocalDate start = LocalDate.now().minusDays(6);
        List<Integer> weeklyVisits = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            weeklyVisits.add(visitsByDay.getOrDefault(start.plusDays(index), 0L).intValue());
        }

        PlaceMaster topSpot = spots.stream().max(Comparator.comparingInt(this::visitCount)).orElse(null);
        int topSpotVisits = topSpot != null ? visitCount(topSpot) : 0;
        DayOfWeek busiestDay = visits.stream()
            .filter(visit -> visit.getVisitDate() != null)
            .collect(Collectors.groupingBy(visit -> visit.getVisitDate().getDayOfWeek(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(DayOfWeek.SATURDAY);

        return VendorAnalyticsDto.builder()
            .totalVisitedMarks(visits.size())
            .visitedChangePct(0)
            .peakVisitWindow("2-4p")
            .peakVisitCount(topSpotVisits)
            .totalUserImages(totalUserImages(spots))
            .totalReflections(reflections.size())
            .topSpotName(topSpot != null ? topSpot.getName() : "No spot yet")
            .topSpotVisits(topSpotVisits)
            .weeklyVisits(weeklyVisits)
            .recentImages(recentImages(spots))
            .recentReflections(reflections.stream().limit(5).map(this::toReflectionDto).toList())
            .uniqueExplorers((int) visits.stream().map(visit -> visit.getAccount().getId()).distinct().count())
            .explorerChangePct(0)
            .busiestDay(busiestDay.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
            .busiestTime("2-4p")
            .collectionInsights(collectionInsights(spots))
            .announcements(defaultAnnouncements())
            .activityItems(activityItems(visits, reflections))
            .build();
    }

    @Override
    @Transactional
    public void submitEditSuggestion(VendorEditSuggestionRequestDto requestDto) throws TanaException {
        PlaceMaster spot = requireSpot(requestDto.getSpotId());
        requireText(requestDto.getMessage());
        createReviewNotification(
            "Partner edit suggestion",
            spot.getName() + " / " + safe(requestDto.getField(), "factual") + ": " + requestDto.getMessage(),
            Boolean.TRUE.equals(requestDto.getUrgent()) ? "VENDOR_URGENT_EDIT" : "VENDOR_EDIT_SUGGESTION"
        );
    }

    @Override
    @Transactional
    public void submitSeasonalFlags(VendorSeasonalFlagsRequestDto requestDto) throws TanaException {
        PlaceMaster spot = requireSpot(requestDto.getSpotId());
        String summary = "closed=" + Boolean.TRUE.equals(requestDto.getTemporarilyClosed())
            + ", dates=" + safe(requestDto.getClosedFrom(), "") + " to " + safe(requestDto.getClosedTo(), "")
            + ", holidayHours=" + safe(requestDto.getHolidayHours(), "")
            + ", offering=" + safe(requestDto.getSeasonalOffering(), "");
        createReviewNotification("Partner seasonal flags", spot.getName() + " / " + summary, "VENDOR_SEASONAL_FLAGS");
    }

    @Override
    @Transactional
    public VendorScanResultDto previewScan(VendorScanRequestDto requestDto) throws TanaException {
        PlaceMaster spot = requireSpot(requestDto.getSpotId());
        AccountMaster explorer = resolveExplorer(requestDto.getExplorerQrToken());
        SpotVisited existing = spotVisitedRepository.findSpotsVisitedByPlaceAndAccountId(explorer.getId(), spot.getId());
        if (existing != null && Boolean.TRUE.equals(existing.getIsVisited())) {
            return scanResult("already_stamped", explorer, spot, "Already stamped for " + firstCollectionName(spot));
        }
        return scanResult("ready", explorer, spot, "Did " + displayName(explorer) + " make a purchase?");
    }

    @Override
    @Transactional
    public VendorScanResultDto confirmScan(VendorScanRequestDto requestDto) throws TanaException {
        PlaceMaster spot = requireSpot(requestDto.getSpotId());
        AccountMaster explorer = resolveExplorer(requestDto.getExplorerQrToken());
        SpotVisited existing = spotVisitedRepository.findSpotsVisitedByPlaceAndAccountId(explorer.getId(), spot.getId());
        if (existing == null) {
            existing = new SpotVisited();
            existing.setAccount(explorer);
            existing.setPlace(spot);
        } else if (Boolean.TRUE.equals(existing.getIsVisited())) {
            return scanResult("already_stamped", explorer, spot, "Already stamped for " + firstCollectionName(spot));
        }
        existing.setIsVisited(true);
        existing.setVisitDate(LocalDate.now());
        spotVisitedRepository.save(existing);
        return scanResult("confirmed", explorer, spot, "Stamp issued.");
    }

    private VendorScanResultDto scanResult(String status, AccountMaster explorer, PlaceMaster spot, String message) {
        return VendorScanResultDto.builder()
            .status(status)
            .explorerName(displayName(explorer))
            .collectionName(firstCollectionName(spot))
            .progressLabel(firstCollectionName(spot) + ("confirmed".equals(status) ? ": stamp issued" : ""))
            .message(message)
            .build();
    }

    private AccountMaster currentAccount() throws TanaException {
        return accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
    }

    private List<PlaceMaster> vendorPlaces() {
        Long vendorAccountId = authConfig.getCurrentUserId();
        List<PlaceMaster> ownedPlaces = vendorPlaceOwnershipRepository
            .findActiveByVendorAccountId(vendorAccountId)
            .stream()
            .map(VendorPlaceOwnership::getPlace)
            .toList();

        if (!ownedPlaces.isEmpty() || vendorPlaceOwnershipRepository.existsByVendorAccountId(vendorAccountId)) {
            return ownedPlaces;
        }

        return placesRepository.findAll();
    }

    private VendorSpotDto toSpotDto(PlaceMaster spot) {
        List<String> images = commonUtils.getSpotImages(spot.getName());
        return VendorSpotDto.builder()
            .spotId(spot.getId())
            .name(spot.getName())
            .category(categoryLabel(spot))
            .town(safe(spot.getTown(), "Bohol"))
            .image(images.isEmpty() ? null : images.get(images.size() - 1))
            .visits(visitCount(spot))
            .visitsThisWeek(visitsThisWeek(spot))
            .reflections(reflectionCount(spot))
            .photoUploads(images.size())
            .saveCount(saveCount(spot))
            .peakVisitLabel("2-4p")
            .status(Boolean.TRUE.equals(spot.getIsTanaVerified()) ? "live" : "draft")
            .overview(spot.getOverview())
            .tanaTip(spot.getTanaTip())
            .openingDays(spot.getOpeningDays())
            .openingHours(spot.getOpeningHours())
            .transportSummary(safe(spot.getGoogleAddress(), spot.getGpsLocation()))
            .includedCollections(collectionNames(spot))
            .build();
    }

    private VendorOwnershipDto toOwnershipDto(VendorPlaceOwnership ownership) {
        return VendorOwnershipDto.builder()
            .ownershipId(ownership.getOwnershipId())
            .vendorAccountId(ownership.getVendorAccount().getId())
            .placeId(ownership.getPlace().getId())
            .placeName(ownership.getPlace().getName())
            .role(safe(ownership.getRole(), "owner"))
            .status(safe(ownership.getStatus(), "active"))
            .build();
    }

    private AdminVendorDto toAdminVendorDto(VendorPlaceOwnership ownership) {
        AccountMaster vendor = ownership.getVendorAccount();
        PlaceMaster place = ownership.getPlace();
        return AdminVendorDto.builder()
            .vendorAccountId(vendor.getId())
            .vendorName(displayName(vendor))
            .email(vendor.getEmail())
            .ownershipId(ownership.getOwnershipId())
            .placeId(place.getId())
            .placeName(place.getName())
            .role(safe(ownership.getRole(), "owner"))
            .status(safe(ownership.getStatus(), "active"))
            .build();
    }

    private VendorApplicationResponseDto toApplicationDto(VendorApplication application) {
        return VendorApplicationResponseDto.builder()
            .applicationId(application.getApplicationId())
            .spotName(application.getSpotName())
            .ownerName(application.getOwnerName())
            .phone(application.getPhone())
            .email(application.getEmail())
            .category(application.getCategory())
            .location(application.getLocation())
            .status(application.getStatus())
            .linkedPlaceId(application.getLinkedPlace() == null ? null : application.getLinkedPlace().getId())
            .reviewNote(application.getReviewNote())
            .build();
    }

    private VendorInviteResponseDto toInviteDto(VendorInvite invite) {
        boolean expired = isInviteExpired(invite);
        return VendorInviteResponseDto.builder()
            .inviteId(invite.getInviteId())
            .token(invite.getToken())
            .email(invite.getEmail())
            .displayName(invite.getDisplayName())
            .placeId(invite.getPlace().getId())
            .placeName(invite.getPlace().getName())
            .status(expired && "pending".equalsIgnoreCase(invite.getStatus()) ? "expired" : invite.getStatus())
            .expiresAt(invite.getExpiresAt().toString())
            .expired(expired)
            .build();
    }

    private VendorInvite requireInvite(String token) throws TanaException {
        return vendorInviteRepository.findByToken(requireText(token))
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
    }

    private boolean isInviteExpired(VendorInvite invite) {
        return invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private String createVendorUsername(String email) {
        String base = Optional.ofNullable(email)
            .filter(value -> value.contains("@"))
            .map(value -> value.substring(0, value.indexOf("@")))
            .orElse("vendor")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]", "");

        if (base.isBlank()) {
            base = "vendor";
        }

        String username = base;
        int suffix = 1;
        while (accountMasterRepository.findByUserName(username) != null) {
            username = base + "_vendor" + suffix;
            suffix++;
        }

        return username.length() > 64 ? username.substring(0, 64) : username;
    }

    private VendorReflectionDto toReflectionDto(Reflection reflection) {
        return VendorReflectionDto.builder()
            .reflectionId(reflection.getReflectionId())
            .spotId(reflection.getPlace().getId())
            .spotName(reflection.getPlace().getName())
            .userName(displayName(reflection.getAccount()))
            .content(reflection.getContent())
            .date(reflection.getCreatedAt() == null ? "" : reflection.getCreatedAt().toLocalDate().format(SHORT_DATE))
            .image(reflection.getImage())
            .build();
    }

    private List<VendorUserImageDto> recentImages(List<PlaceMaster> spots) {
        List<VendorUserImageDto> images = new ArrayList<>();
        for (PlaceMaster spot : spots) {
            List<String> spotImages = commonUtils.getSpotImages(spot.getName());
            for (int index = 0; index < spotImages.size(); index++) {
                images.add(VendorUserImageDto.builder()
                    .imageId((spot.getId() * 1000) + index)
                    .spotId(spot.getId())
                    .spotName(spot.getName())
                    .image(spotImages.get(index))
                    .date("")
                    .build());
            }
        }
        return images.stream().limit(4).toList();
    }

    private List<VendorCollectionInsightDto> collectionInsights(List<PlaceMaster> spots) {
        Map<Long, CollectionsMaster> collections = spots.stream()
            .flatMap(spot -> Optional.ofNullable(spot.getCollections()).orElse(List.of()).stream())
            .map(CollectionsCategorySelections::getCollection)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(CollectionsMaster::getCollectionId, Function.identity(), (left, right) -> left));
        return collections.values().stream()
            .map(collection -> VendorCollectionInsightDto.builder()
                .collectionId(collection.getCollectionId())
                .collectionName(collection.getCollectionName())
                .spotCount(Optional.ofNullable(collection.getCollection()).orElse(List.of()).size())
                .completionRate(0)
                .usualPosition("Usually 2nd")
                .build())
            .limit(4)
            .toList();
    }

    private List<VendorAnnouncementDto> defaultAnnouncements() {
        return List.of(VendorAnnouncementDto.builder()
            .announcementId(1L)
            .date(LocalDate.now().format(SHORT_DATE))
            .title("Partner portal is live")
            .body("Use My Spot for factual updates, Scan for stamps, and Insights for quick monthly context.")
            .build());
    }

    private List<VendorActivityItemDto> activityItems(List<SpotVisited> visits, List<Reflection> reflections) {
        List<VendorActivityItemDto> items = new ArrayList<>();
        visits.stream().limit(6).forEach(visit -> items.add(VendorActivityItemDto.builder()
            .activityId(visit.getVisitedId())
            .type("stamp")
            .explorerName(displayName(visit.getAccount()))
            .spotName(visit.getPlace().getName())
            .label("Stamp issued")
            .happenedAt(visit.getVisitDate() == null ? "" : visit.getVisitDate().format(SHORT_DATE))
            .muted(false)
            .build()));
        reflections.stream().limit(4).forEach(reflection -> items.add(VendorActivityItemDto.builder()
            .activityId(100000L + reflection.getReflectionId())
            .type("reflection")
            .explorerName(displayName(reflection.getAccount()))
            .spotName(reflection.getPlace().getName())
            .label(reflection.getContent())
            .happenedAt(reflection.getCreatedAt() == null ? "" : reflection.getCreatedAt().toLocalDate().format(SHORT_DATE))
            .muted(false)
            .build()));
        return items.stream().limit(8).toList();
    }

    private void createReviewNotification(String title, String body, String type) throws TanaException {
        notificationInboxRepository.save(NotificationInbox.builder()
            .account(currentAccount())
            .title(title)
            .body(body)
            .notificationType(type)
            .data(Map.of("source", "vendor_portal"))
            .read(false)
            .createdAt(LocalDateTime.now())
            .build());
    }

    private PlaceMaster requireSpot(Long spotId) throws TanaException {
        if (spotId == null) throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        Long vendorAccountId = authConfig.getCurrentUserId();
        Optional<VendorPlaceOwnership> ownership =
            vendorPlaceOwnershipRepository.findActiveOwnership(vendorAccountId, spotId);

        if (ownership.isPresent()) {
            return ownership.get().getPlace();
        }

        if (vendorPlaceOwnershipRepository.existsByVendorAccountId(vendorAccountId)) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        return placesRepository.findById(spotId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
    }

    private AccountMaster resolveExplorer(String token) throws TanaException {
        String numeric = requireText(token).replace("tana://explorer/", "").replace("explorer:", "").trim();
        try {
            return accountMasterRepository.findById(Long.valueOf(numeric))
                .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        } catch (NumberFormatException exception) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }
    }

    private String requireText(String value) throws TanaException {
        if (ObjectUtils.isEmpty(value) || value.trim().isEmpty()) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
        return value.trim();
    }

    private int visitCount(PlaceMaster spot) {
        return (int) Optional.ofNullable(spot.getVisitors()).orElse(List.of()).stream()
            .filter(visit -> Boolean.TRUE.equals(visit.getIsVisited()))
            .count();
    }

    private int visitsThisWeek(PlaceMaster spot) {
        LocalDate start = LocalDate.now().minusDays(6);
        return (int) Optional.ofNullable(spot.getVisitors()).orElse(List.of()).stream()
            .filter(visit -> Boolean.TRUE.equals(visit.getIsVisited()))
            .filter(visit -> visit.getVisitDate() != null && !visit.getVisitDate().isBefore(start))
            .count();
    }

    private int reflectionCount(PlaceMaster spot) {
        return Optional.ofNullable(spot.getReflections()).orElse(List.of()).size();
    }

    private int saveCount(PlaceMaster spot) {
        return (int) Optional.ofNullable(spot.getUserSaves()).orElse(List.of()).stream()
            .filter(save -> Boolean.TRUE.equals(save.getSaved()))
            .count();
    }

    private int totalUserImages(List<PlaceMaster> spots) {
        return spots.stream().mapToInt(spot -> commonUtils.getSpotImages(spot.getName()).size()).sum();
    }

    private String displayName(AccountMaster account) {
        String first = safe(account.getFirstName(), "");
        String lastInitial = safe(account.getLastName(), "").isBlank()
            ? ""
            : " " + account.getLastName().substring(0, 1) + ".";
        String name = (first + lastInitial).trim();
        return name.isBlank() ? safe(account.getUserName(), account.getEmail()) : name;
    }

    private String categoryLabel(PlaceMaster spot) {
        return spot.getMainCategoryTypeEnum() == null ? "Partner spot" : spot.getMainCategoryTypeEnum().getTypeString();
    }

    private String firstSpotImage(PlaceMaster spot) {
        if (spot == null) return null;
        List<String> images = commonUtils.getSpotImages(spot.getName());
        return images.isEmpty() ? null : images.get(images.size() - 1);
    }

    private String firstCollectionName(PlaceMaster spot) {
        return collectionNames(spot).stream().findFirst().orElse("Tana collection");
    }

    private List<String> collectionNames(PlaceMaster spot) {
        return Optional.ofNullable(spot.getCollections()).orElse(List.of()).stream()
            .map(CollectionsCategorySelections::getCollection)
            .filter(Objects::nonNull)
            .map(CollectionsMaster::getCollectionName)
            .toList();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
