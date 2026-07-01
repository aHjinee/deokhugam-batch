package com.sbproject.deokhugam.domain.notification.service;

import com.sbproject.deokhugam.domain.notification.entity.Notification;
import com.sbproject.deokhugam.domain.notification.entity.NotificationType;
import com.sbproject.deokhugam.domain.notification.repository.NotificationQueryRepository;
import com.sbproject.deokhugam.domain.notification.repository.NotificationRepository;
import com.sbproject.deokhugam.domain.review.entity.Review;
import com.sbproject.deokhugam.domain.review.repository.ReviewRepository;
import com.sbproject.deokhugam.domain.user.entity.User;
import com.sbproject.deokhugam.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public int deleteExpiredConfirmedNotifications() {
        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
        return notificationRepository.deleteConfirmedNotificationsBefore(threshold);
    }

    @Transactional
    public void create(NotificationType type, UUID receiverId, UUID reviewId) {

        String message = createMessage(type);

        User receiveUser = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalStateException(
                        "User not found: " + receiverId));


        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalStateException(
                        "Review not found: " + reviewId));


        Notification notification = Notification.builder()
                .type(type)
                .user(receiveUser)
                .review(review)
                .reviewContent(review.getContent())
                .message(message)
                .confirmed(false)
                .build();

        notificationRepository.save(notification);
    }

    private String createMessage(NotificationType type) {


        return switch (type) {

            case POPULAR_DAILY -> "회원님의 리뷰가 일간 인기 리뷰 TOP10에 선정되었습니다.";

            case POPULAR_WEEKLY -> "회원님의 리뷰가 주간 인기 리뷰 TOP10에 선정되었습니다.";

            case POPULAR_MONTHLY -> "회원님의 리뷰가 월간 인기 리뷰 TOP10에 선정되었습니다.";

            case POPULAR_ALL_TIME -> "회원님의 리뷰가 전체 인기 리뷰 TOP10에 선정되었습니다.";
        };
    }
}
