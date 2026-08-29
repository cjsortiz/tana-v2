package com.tana.tana_auth.functions.notification.repository;

import com.tana.tana_common.model.NotificationInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationInboxRepository extends JpaRepository<NotificationInbox, Long> {

    List<NotificationInbox> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    long countByAccountIdAndReadFalse(Long accountId);

    @Modifying
    @Query("""
        UPDATE NotificationInbox n
        SET n.read = true,
            n.readAt = :readAt
        WHERE n.account.id = :accountId
          AND n.read = false
    """)
    void markAllRead(@Param("accountId") Long accountId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("""
        DELETE FROM NotificationInbox n
        WHERE n.account.id = :accountId
    """)
    void deleteAllByAccountId(@Param("accountId") Long accountId);
}
