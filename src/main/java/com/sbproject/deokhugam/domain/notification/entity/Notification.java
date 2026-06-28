package com.sbproject.deokhugam.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.uuid.Generators;
import com.sbproject.deokhugam.domain.review.entity.Review;
import com.sbproject.deokhugam.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Notification {
    @Id
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null){
            id = Generators.timeBasedEpochGenerator().generate();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "review_content", nullable = false, columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()",
            updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp with time zone default now()")
    private Instant updatedAt;

    public void confirm() {
        this.confirmed = true;
    }
}
