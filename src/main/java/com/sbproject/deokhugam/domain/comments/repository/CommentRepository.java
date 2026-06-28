package com.sbproject.deokhugam.domain.comments.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sbproject.deokhugam.domain.comments.entity.Comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);

	List<Comment> findByReview_IdAndDeletedAtIsNull(UUID reviewId, Pageable pageable);

	List<Comment> findByReview_IdAndDeletedAtIsNullAndCreatedAtLessThan(
		UUID reviewId,
		Instant createdAt,
		Pageable pageable
	);

	List<Comment> findByReview_IdAndDeletedAtIsNullAndCreatedAtGreaterThan(
		UUID reviewId,
		Instant createdAt,
		Pageable pageable
	);

	long countByReview_IdAndDeletedAtIsNull(UUID reviewId);
}
