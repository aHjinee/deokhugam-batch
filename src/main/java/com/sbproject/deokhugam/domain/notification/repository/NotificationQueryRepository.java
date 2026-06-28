package com.sbproject.deokhugam.domain.notification.repository;

import com.sbproject.deokhugam.domain.notification.entity.Notification;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.UUID;

public interface NotificationQueryRepository {
    Slice<Notification> findAllByUserId(UUID userId,
                                UUID cursorId,
                                Instant after,
                                int size);
}
