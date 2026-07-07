package com.sbproject.deokhugam.domain.dashboard.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sbproject.deokhugam.domain.dashboard.document.UserActivityStatsDocument;

public interface UserActivityStatsRepository extends MongoRepository<UserActivityStatsDocument, String> {
	Optional<UserActivityStatsDocument> findByUserIdAndActivityDate(String userId, Instant activityDate);

}