package com.sbproject.deokhugam.domain.notification.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sbproject.deokhugam.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Modifying
    @Query("""
    DELETE FROM Notification n
    WHERE n.confirmed = true
      AND n.updatedAt <= :threshold
""")
    int deleteConfirmedNotificationsBefore(@Param("threshold") Instant threshold);
}
