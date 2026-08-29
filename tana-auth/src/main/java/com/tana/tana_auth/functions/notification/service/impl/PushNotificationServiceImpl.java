package com.tana.tana_auth.functions.notification.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.notification.dto.NotificationInboxRequestDto;
import com.tana.tana_auth.functions.notification.dto.NotificationInboxResponseDto;
import com.tana.tana_auth.functions.notification.dto.PushNotificationRequestDto;
import com.tana.tana_auth.functions.notification.dto.PushNotificationResponseDto;
import com.tana.tana_auth.functions.notification.dto.PushTokenRequestDto;
import com.tana.tana_auth.functions.notification.repository.NotificationInboxRepository;
import com.tana.tana_auth.functions.notification.repository.PushNotificationTokenRepository;
import com.tana.tana_auth.functions.notification.service.PushNotificationService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.DevicePlatformEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.NotificationInbox;
import com.tana.tana_common.model.PushNotificationToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final int FCM_BATCH_SIZE = 500;
    private static final int EXPO_BATCH_SIZE = 100;
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushNotificationTokenRepository pushNotificationTokenRepository;

    private final NotificationInboxRepository notificationInboxRepository;

    private final AccountMasterRepository accountMasterRepository;

    private final AuthConfig authConfig;

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    @Transactional
    public void registerToken(PushTokenRequestDto requestDto) throws TanaException {
        if (ObjectUtils.isEmpty(requestDto.getToken()) || ObjectUtils.isEmpty(requestDto.getPlatform())) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        AccountMaster account = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        PushNotificationToken token = pushNotificationTokenRepository.findByToken(requestDto.getToken())
            .orElseGet(PushNotificationToken::new);

        LocalDateTime now = LocalDateTime.now();
        token.setAccount(account);
        token.setToken(requestDto.getToken());
        token.setPlatform(requestDto.getPlatform());
        token.setDeviceId(requestDto.getDeviceId());
        token.setEnabled(true);
        token.setUpdatedAt(now);

        if (token.getCreatedAt() == null) {
            token.setCreatedAt(now);
        }

        pushNotificationTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void deleteToken(PushTokenRequestDto requestDto) {
        if (!ObjectUtils.isEmpty(requestDto.getToken())) {
            pushNotificationTokenRepository.deleteByToken(requestDto.getToken());
        }
    }

    @Override
    public PushNotificationResponseDto sendNotification(PushNotificationRequestDto requestDto) throws TanaException {
        List<PushNotificationToken> tokens = ObjectUtils.isEmpty(requestDto.getAccountId())
            ? pushNotificationTokenRepository.findByEnabledTrue()
            : pushNotificationTokenRepository.findByAccountIdAndEnabledTrue(requestDto.getAccountId());

        if (tokens.isEmpty()) {
            return PushNotificationResponseDto.builder()
                .requestedCount(0)
                .successCount(0)
                .failureCount(0)
                .build();
        }

        List<PushNotificationToken> expoTokens = tokens.stream()
            .filter(this::isExpoToken)
            .toList();
        List<PushNotificationToken> firebaseTokens = tokens.stream()
            .filter(token -> !isExpoToken(token))
            .toList();

        PushNotificationResponseDto expoResponse = sendExpoNotifications(requestDto, expoTokens);
        PushNotificationResponseDto firebaseResponse = sendFirebaseNotifications(requestDto, firebaseTokens);

        return PushNotificationResponseDto.builder()
            .requestedCount(tokens.size())
            .successCount(expoResponse.getSuccessCount() + firebaseResponse.getSuccessCount())
            .failureCount(expoResponse.getFailureCount() + firebaseResponse.getFailureCount())
            .build();
    }

    @Override
    @Transactional
    public NotificationInboxResponseDto saveNotification(NotificationInboxRequestDto requestDto) throws TanaException {
        if (ObjectUtils.isEmpty(requestDto.getTitle()) || ObjectUtils.isEmpty(requestDto.getBody())) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        AccountMaster account = accountMasterRepository.findById(authConfig.getCurrentUserId())
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        NotificationInbox notification = NotificationInbox.builder()
            .account(account)
            .title(requestDto.getTitle())
            .body(requestDto.getBody())
            .notificationType(requestDto.getNotificationType())
            .data(requestDto.getData())
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();

        return toNotificationResponse(notificationInboxRepository.save(notification));
    }

    @Override
    public List<NotificationInboxResponseDto> fetchNotifications() {
        return notificationInboxRepository.findByAccountIdOrderByCreatedAtDesc(authConfig.getCurrentUserId())
            .stream()
            .map(this::toNotificationResponse)
            .toList();
    }

    @Override
    public long getUnreadCount() {
        return notificationInboxRepository.countByAccountIdAndReadFalse(authConfig.getCurrentUserId());
    }

    @Override
    @Transactional
    public void markNotificationRead(Long notificationId) throws TanaException {
        NotificationInbox notification = notificationInboxRepository.findById(notificationId)
            .orElseThrow(() -> new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));

        if (!notification.getAccount().getId().equals(authConfig.getCurrentUserId())) {
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationInboxRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllNotificationsRead() {
        notificationInboxRepository.markAllRead(authConfig.getCurrentUserId(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public void clearNotifications() {
        notificationInboxRepository.deleteAllByAccountId(authConfig.getCurrentUserId());
    }

    private NotificationInboxResponseDto toNotificationResponse(NotificationInbox notification) {
        return NotificationInboxResponseDto.builder()
            .notificationId(notification.getNotificationId())
            .title(notification.getTitle())
            .body(notification.getBody())
            .notificationType(notification.getNotificationType())
            .read(notification.isRead())
            .data(notification.getData())
            .createdAt(notification.getCreatedAt())
            .readAt(notification.getReadAt())
            .build();
    }

    private boolean isExpoToken(PushNotificationToken token) {
        return token.getPlatform() == DevicePlatformEnum.EXPO
            || token.getToken().startsWith("ExpoPushToken")
            || token.getToken().startsWith("ExponentPushToken");
    }

    private PushNotificationResponseDto sendFirebaseNotifications(
        PushNotificationRequestDto requestDto,
        List<PushNotificationToken> tokens
    ) throws TanaException {
        if (tokens.isEmpty()) {
            return PushNotificationResponseDto.builder()
                .requestedCount(0)
                .successCount(0)
                .failureCount(0)
                .build();
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            return PushNotificationResponseDto.builder()
                .requestedCount(tokens.size())
                .successCount(0)
                .failureCount(tokens.size())
                .build();
        }

        int successCount = 0;
        int failureCount = 0;

        for (int start = 0; start < tokens.size(); start += FCM_BATCH_SIZE) {
            List<PushNotificationToken> batch = tokens.subList(start, Math.min(start + FCM_BATCH_SIZE, tokens.size()));
            BatchResponse response;
            try {
                response = firebaseMessaging.sendEachForMulticast(buildMessage(requestDto, batch));
            } catch (FirebaseMessagingException e) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }
            successCount += response.getSuccessCount();
            failureCount += response.getFailureCount();
            disableExpiredTokens(batch, response.getResponses());
        }

        return PushNotificationResponseDto.builder()
            .requestedCount(tokens.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .build();
    }

    private PushNotificationResponseDto sendExpoNotifications(
        PushNotificationRequestDto requestDto,
        List<PushNotificationToken> tokens
    ) throws TanaException {
        int successCount = 0;
        int failureCount = 0;

        for (int start = 0; start < tokens.size(); start += EXPO_BATCH_SIZE) {
            List<PushNotificationToken> batch = tokens.subList(start, Math.min(start + EXPO_BATCH_SIZE, tokens.size()));

            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EXPO_PUSH_URL))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(buildExpoPayload(requestDto, batch))))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    failureCount += batch.size();
                    continue;
                }

                ExpoPushResult result = parseExpoResponse(batch, response.body());
                successCount += result.successCount();
                failureCount += result.failureCount();
                disableExpiredExpoTokens(result.expiredTokens());
            } catch (Exception e) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }
        }

        return PushNotificationResponseDto.builder()
            .requestedCount(tokens.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .build();
    }

    private List<Map<String, Object>> buildExpoPayload(
        PushNotificationRequestDto requestDto,
        List<PushNotificationToken> tokens
    ) {
        return tokens.stream()
            .map(token -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("to", token.getToken());
                payload.put("title", requestDto.getTitle());
                payload.put("body", requestDto.getBody());
                payload.put("sound", "default");
                payload.put("priority", "high");

                if (requestDto.getData() != null && !requestDto.getData().isEmpty()) {
                    payload.put("data", requestDto.getData());
                }

                return payload;
            })
            .toList();
    }

    private ExpoPushResult parseExpoResponse(List<PushNotificationToken> tokens, String responseBody) throws Exception {
        JsonNode data = objectMapper.readTree(responseBody).path("data");
        int successCount = 0;
        int failureCount = 0;
        List<PushNotificationToken> expiredTokens = new ArrayList<>();

        if (!data.isArray()) {
            return new ExpoPushResult(0, tokens.size(), expiredTokens);
        }

        for (int i = 0; i < tokens.size(); i++) {
            JsonNode item = data.get(i);
            String status = item != null ? item.path("status").asText() : "error";

            if ("ok".equalsIgnoreCase(status)) {
                successCount++;
                continue;
            }

            failureCount++;
            String error = item != null ? item.path("details").path("error").asText() : "";
            if ("DeviceNotRegistered".equals(error)) {
                PushNotificationToken token = tokens.get(i);
                token.setEnabled(false);
                token.setUpdatedAt(LocalDateTime.now());
                expiredTokens.add(token);
            }
        }

        return new ExpoPushResult(successCount, failureCount, expiredTokens);
    }

    @Transactional
    void disableExpiredExpoTokens(List<PushNotificationToken> expiredTokens) {
        if (!expiredTokens.isEmpty()) {
            pushNotificationTokenRepository.saveAll(expiredTokens);
        }
    }

    private MulticastMessage buildMessage(PushNotificationRequestDto requestDto, List<PushNotificationToken> tokens) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
            .setNotification(Notification.builder()
                .setTitle(requestDto.getTitle())
                .setBody(requestDto.getBody())
                .build())
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setSound("default")
                    .build())
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .setAps(Aps.builder()
                    .setContentAvailable(true)
                    .setSound("default")
                    .build())
                .build())
            .addAllTokens(tokens.stream()
                .map(PushNotificationToken::getToken)
                .toList());

        Map<String, String> data = requestDto.getData();
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }

    @Transactional
    void disableExpiredTokens(List<PushNotificationToken> tokens, List<SendResponse> responses) {
        List<PushNotificationToken> expiredTokens = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (response.isSuccessful() || response.getException() == null) {
                continue;
            }

            FirebaseMessagingException exception = response.getException();
            MessagingErrorCode errorCode = exception.getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                PushNotificationToken token = tokens.get(i);
                token.setEnabled(false);
                token.setUpdatedAt(LocalDateTime.now());
                expiredTokens.add(token);
            }
        }

        if (!expiredTokens.isEmpty()) {
            pushNotificationTokenRepository.saveAll(expiredTokens);
        }
    }

    private record ExpoPushResult(
        int successCount,
        int failureCount,
        List<PushNotificationToken> expiredTokens
    ) {
    }
}
