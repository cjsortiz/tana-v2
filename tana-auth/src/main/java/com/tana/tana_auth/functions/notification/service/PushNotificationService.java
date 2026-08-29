package com.tana.tana_auth.functions.notification.service;

import com.tana.tana_auth.functions.notification.dto.PushNotificationRequestDto;
import com.tana.tana_auth.functions.notification.dto.PushNotificationResponseDto;
import com.tana.tana_auth.functions.notification.dto.PushTokenRequestDto;
import com.tana.tana_auth.functions.notification.dto.NotificationInboxRequestDto;
import com.tana.tana_auth.functions.notification.dto.NotificationInboxResponseDto;
import com.tana.tana_common.constant.exception.TanaException;

import java.util.List;

public interface PushNotificationService {

    void registerToken(PushTokenRequestDto requestDto) throws TanaException;

    void deleteToken(PushTokenRequestDto requestDto);

    PushNotificationResponseDto sendNotification(PushNotificationRequestDto requestDto) throws TanaException;

    NotificationInboxResponseDto saveNotification(NotificationInboxRequestDto requestDto) throws TanaException;

    List<NotificationInboxResponseDto> fetchNotifications() throws TanaException;

    long getUnreadCount();

    void markNotificationRead(Long notificationId) throws TanaException;

    void markAllNotificationsRead();

    void clearNotifications();
}
