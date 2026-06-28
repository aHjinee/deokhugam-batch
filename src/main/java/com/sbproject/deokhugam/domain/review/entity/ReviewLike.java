package com.sbproject.deokhugam.domain.review.entity;
import com.sbproject.deokhugam.domain.user.entity.User;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.uuid.Generators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_likes")
@Getter
@SuperBuilder
@ToString(exclude = "review")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {
	@Id
	@Column(updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = Generators.timeBasedEpochGenerator().generate();
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;  // 좋아요 누른 사용자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", nullable = false)
	@JsonIgnore
	private Review review;  // 좋아요 대상 게시글

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false,
		columnDefinition = "timestamp with time zone default now()")
	private Instant createdAt;
}
