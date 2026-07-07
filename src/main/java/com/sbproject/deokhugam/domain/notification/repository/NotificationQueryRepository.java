package com.sbproject.deokhugam.domain.notification.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.sbproject.deokhugam.domain.notification.entity.Notification;

public interface NotificationQueryRepository {
    Slice<Notification> findAllByUserId(UUID userId,
                                UUID cursorId,
                                Instant after,
                                int size);
}
