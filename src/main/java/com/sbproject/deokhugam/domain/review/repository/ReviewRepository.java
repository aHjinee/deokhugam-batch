package com.sbproject.deokhugam.domain.review.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbproject.deokhugam.domain.review.entity.Review;
import com.sbproject.deokhugam.domain.review.repository.querydsl.ReviewQueryRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewQueryRepository {
}
