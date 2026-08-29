package com.tana.tana_auth.functions.notification.controller;

import com.tana.tana_auth.functions.notification.dto.PushNotificationRequestDto;
import com.tana.tana_auth.functions.notification.dto.PushTokenRequestDto;
import com.tana.tana_auth.functions.notification.dto.NotificationInboxRequestDto;
import com.tana.tana_auth.functions.notification.service.PushNotificationService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/notifications")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping(value = "/tokens")
    public TanaApiResponse registerToken(@RequestAttribute("validated") PushTokenRequestDto requestDto)
        throws TanaException {
        pushNotificationService.registerToken(requestDto);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }

    @DeleteMapping(value = "/tokens")
    public TanaApiResponse deleteToken(@RequestAttribute("validated") PushTokenRequestDto requestDto) {
        pushNotificationService.deleteToken(requestDto);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }

    @PostMapping(value = "/send")
    public TanaApiResponse sendNotification(@RequestAttribute("validated") PushNotificationRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(pushNotificationService.sendNotification(requestDto))
            .build();
    }

    @PostMapping(value = "/inbox")
    public TanaApiResponse saveNotification(@RequestAttribute("validated") NotificationInboxRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(pushNotificationService.saveNotification(requestDto))
            .build();
    }

    @GetMapping(value = "/inbox")
    public TanaApiResponse fetchNotifications() throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(pushNotificationService.fetchNotifications())
            .build();
    }

    @GetMapping(value = "/unread-count")
    public TanaApiResponse getUnreadCount() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(pushNotificationService.getUnreadCount())
            .build();
    }

    @PutMapping(value = "/inbox/{notificationId}/read")
    public TanaApiResponse markNotificationRead(@PathVariable("notificationId") Long notificationId)
        throws TanaException {
        pushNotificationService.markNotificationRead(notificationId);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }

    @PutMapping(value = "/inbox/read-all")
    public TanaApiResponse markAllNotificationsRead() {
        pushNotificationService.markAllNotificationsRead();
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }

    @DeleteMapping(value = "/inbox")
    public TanaApiResponse clearNotifications() {
        pushNotificationService.clearNotifications();
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }
}
