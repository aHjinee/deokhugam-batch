package com.sbproject.deokhugam.domain.notification.service;

import com.sbproject.deokhugam.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public int deleteExpiredConfirmedNotifications() {
        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
        return notificationRepository.deleteConfirmedNotificationsBefore(threshold);
    }
}
