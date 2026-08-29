package com.tana.tana_auth.functions.notification.repository;

import com.tana.tana_common.model.PushNotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushNotificationTokenRepository extends JpaRepository<PushNotificationToken, Long> {

    Optional<PushNotificationToken> findByToken(String token);

    List<PushNotificationToken> findByAccountIdAndEnabledTrue(Long accountId);

    List<PushNotificationToken> findByEnabledTrue();

    void deleteByToken(String token);
}
