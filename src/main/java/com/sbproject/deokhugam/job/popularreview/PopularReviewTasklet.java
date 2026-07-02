package com.sbproject.deokhugam.job.popularreview;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularReviewsRepository;
import com.sbproject.deokhugam.domain.notification.entity.NotificationType;
import com.sbproject.deokhugam.domain.notification.service.NotificationService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PopularReviewTasklet implements Tasklet {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JdbcTemplate jdbcTemplate;
	private final PopularReviewsRepository popularReviewsRepository;
	private final BatchMetrics batchMetrics;
	private final NotificationService notificationService;


	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		LocalDate today = LocalDate.now(SEOUL_ZONE);

		for (PeriodType periodType : PeriodType.values()) {
			Instant startAt = getStartAt(periodType, today);
			Instant endAt = getEndAt(today);


			Timestamp startTimestamp = Timestamp.from(startAt);
			Timestamp endTimestamp = Timestamp.from(endAt);

			List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"""
				SELECT
					r.id AS review_id,
					r.user_id,
					r.book_id,
					u.nickname,
					b.title,
					b.thumbnail_url,
					r.content,
					r.rating,
					r.created_at,
					COUNT(DISTINCT rl.id) AS like_count,
					COUNT(DISTINCT c.id) AS comment_count
				FROM reviews r
				JOIN users u ON u.id = r.user_id
				JOIN books b ON b.id = r.book_id
				LEFT JOIN review_likes rl ON rl.review_id = r.id
					AND rl.created_at >= ? AND rl.created_at < ?
				LEFT JOIN comments c ON c.review_id = r.id
					AND c.created_at >= ? AND c.created_at < ?
				WHERE r.deleted_at IS NULL
				GROUP BY
					r.id,
					r.user_id,
					r.book_id,
					u.nickname,
					b.title,
					b.thumbnail_url,
					r.content,
					r.rating,
					r.created_at
				HAVING (
					COUNT(DISTINCT rl.id) * 0.3
					+ COUNT(DISTINCT c.id) * 0.7
				) > 0
				ORDER BY (
					COUNT(DISTINCT rl.id) * 0.3
					+ COUNT(DISTINCT c.id) * 0.7
				) DESC
				LIMIT 20
				""",
				startTimestamp,
				endTimestamp,
				startTimestamp,
				endTimestamp
			);

			List<PopularReviewsDocument.Ranking> rankings = new ArrayList<>();

			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> row = rows.get(i);

				long likeCount =
					((Number) row.get("like_count")).longValue();
				long commentCount =
					((Number) row.get("comment_count")).longValue();

				double score =
					likeCount * 0.3 + commentCount * 0.7;

				rankings.add(new PopularReviewsDocument.Ranking(
					i + 1,
					row.get("review_id").toString(),
					row.get("user_id").toString(),
					row.get("book_id").toString(),
					row.get("nickname").toString(),
					row.get("title").toString(),
					row.get("thumbnail_url") != null
						? row.get("thumbnail_url").toString()
						: null,
					row.get("content").toString(),
					((Number) row.get("rating")).doubleValue(),
					score,
					(int) likeCount,
					(int) commentCount,
					((Timestamp) row.get("created_at")).toInstant()
				));
			}

			NotificationType notificationType = switch (periodType) {
				case DAILY -> NotificationType.POPULAR_DAILY;
				case WEEKLY -> NotificationType.POPULAR_WEEKLY;
				case MONTHLY -> NotificationType.POPULAR_MONTHLY;
				case ALL_TIME -> NotificationType.POPULAR_ALL_TIME;
			};

			for (int i = 0; i < Math.min(10, rankings.size()); i++) {
				PopularReviewsDocument.Ranking ranking = rankings.get(i);

				notificationService.create(
						notificationType,
						UUID.fromString(ranking.getUserId()),
						UUID.fromString(ranking.getReviewId())
				);
			}

			Instant now = Instant.now();
			Instant periodDate = today
				.atStartOfDay(SEOUL_ZONE)
				.toInstant();

			popularReviewsRepository
				.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					document -> {
						document.update(rankings, now);
						popularReviewsRepository.save(document);
					},
					() -> popularReviewsRepository.save(
						PopularReviewsDocument.create(
							periodType,
							periodDate,
							rankings,
							now
						)
					)
				);

			batchMetrics.recordProcessedItems(
				"popularReview",
				periodType.name(),
				rankings.size()
			);

		}

		return RepeatStatus.FINISHED;
	}

	private Instant getStartAt(
		PeriodType periodType,
		LocalDate today
	) {
		return switch (periodType) {
			case DAILY ->
			    today.minusDays(1).atStartOfDay(SEOUL_ZONE).toInstant();
			case WEEKLY ->
				today.minusWeeks(1).atStartOfDay(SEOUL_ZONE).toInstant();
			case MONTHLY ->
				today.minusMonths(1).atStartOfDay(SEOUL_ZONE).toInstant();
			case ALL_TIME -> Instant.EPOCH;
		};
	}

	private Instant getEndAt(LocalDate today) {
		return today.atStartOfDay(SEOUL_ZONE).toInstant();
	}
}