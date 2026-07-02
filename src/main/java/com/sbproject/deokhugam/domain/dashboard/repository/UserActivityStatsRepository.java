package com.sbproject.deokhugam.domain.dashboard.repository;

import com.sbproject.deokhugam.domain.dashboard.document.UserActivityStatsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserActivityStatsRepository extends MongoRepository<UserActivityStatsDocument, String> {
	Optional<UserActivityStatsDocument> findByUserIdAndActivityDate(String userId, Instant activityDate);

}