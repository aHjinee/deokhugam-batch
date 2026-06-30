package com.sbproject.deokhugam.job.useractivitystats;

import com.sbproject.deokhugam.domain.dashboard.document.UserActivityStatsDocument;
import com.sbproject.deokhugam.domain.dashboard.repository.UserActivityStatsRepository;
import com.sbproject.deokhugam.monitoring.BatchMetrics;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserActivityStatsTasklet implements Tasklet {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JdbcTemplate jdbcTemplate;
	private final UserActivityStatsRepository userActivityStatsRepository;
	private final BatchMetrics batchMetrics;

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		LocalDate today = LocalDate.now(SEOUL_ZONE);

		Instant activityDate = today
			.atStartOfDay(SEOUL_ZONE)
			.toInstant();

		Timestamp startTs = Timestamp.from(activityDate);

		Timestamp endTs = Timestamp.from(
			today.plusDays(1)
				.atStartOfDay(SEOUL_ZONE)
				.toInstant()
		);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"""
			SELECT
				u.id AS user_id,
				COUNT(DISTINCT r.id) AS review_count,
				COUNT(DISTINCT c.id) AS comment_count,
				COUNT(DISTINCT rl.id) AS like_count,
				COUNT(DISTINCT rc.id) AS received_comment_count,
				COUNT(DISTINCT rrl.id) AS received_like_count
			FROM users u
			LEFT JOIN reviews r
				ON r.user_id = u.id
				AND r.created_at >= ?
				AND r.created_at < ?
			LEFT JOIN comments c
				ON c.user_id = u.id
				AND c.created_at >= ?
				AND c.created_at < ?
			LEFT JOIN review_likes rl
				ON rl.user_id = u.id
				AND rl.created_at >= ?
				AND rl.created_at < ?
			LEFT JOIN reviews my_r
				ON my_r.user_id = u.id
			LEFT JOIN comments rc
				ON rc.review_id = my_r.id
				AND rc.created_at >= ?
				AND rc.created_at < ?
			LEFT JOIN review_likes rrl
				ON rrl.review_id = my_r.id
				AND rrl.created_at >= ?
				AND rrl.created_at < ?
			WHERE u.deleted_at IS NULL
			GROUP BY u.id
			HAVING (
				COUNT(DISTINCT r.id) > 0
				OR COUNT(DISTINCT c.id) > 0
				OR COUNT(DISTINCT rl.id) > 0
				OR COUNT(DISTINCT rc.id) > 0
				OR COUNT(DISTINCT rrl.id) > 0
			)
			ORDER BY u.id
			""",
			startTs, endTs,
			startTs, endTs,
			startTs, endTs,
			startTs, endTs,
			startTs, endTs
		);

		Instant now = Instant.now();

		for (Map<String, Object> row : rows) {
			String userId = row.get("user_id").toString();

			int reviewCount =
				((Number) row.get("review_count")).intValue();

			int commentCount =
				((Number) row.get("comment_count")).intValue();

			int likeCount =
				((Number) row.get("like_count")).intValue();

			int receivedCommentCount =
				((Number) row.get("received_comment_count")).intValue();

			int receivedLikeCount =
				((Number) row.get("received_like_count")).intValue();

			userActivityStatsRepository
				.findByUserIdAndActivityDate(userId, activityDate)
				.ifPresentOrElse(
					document -> {
						document.update(
							reviewCount,
							commentCount,
							likeCount,
							receivedCommentCount,
							receivedLikeCount,
							now
						);

						userActivityStatsRepository.save(document);
					},
					() -> userActivityStatsRepository.save(
						UserActivityStatsDocument.create(
							userId,
							activityDate,
							reviewCount,
							commentCount,
							likeCount,
							receivedCommentCount,
							receivedLikeCount,
							now
						)
					)
				);
		}
		batchMetrics.recordProcessedItems(
			"userActivityStats",
			"DAILY",
			rows.size()
		);


		return RepeatStatus.FINISHED;
	}
}