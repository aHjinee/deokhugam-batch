package com.sbproject.deokhugam.job.poweruser;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.document.PowerUsersDocument;
import com.sbproject.deokhugam.domain.dashboard.document.UserActivityStatsDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularReviewsRepository;
import com.sbproject.deokhugam.domain.dashboard.repository.PowerUsersRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PowerUserTasklet implements Tasklet {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JdbcTemplate jdbcTemplate;
	private final PowerUsersRepository powerUsersRepository;
	private final PopularReviewsRepository popularReviewsRepository;
	private final UserActivityStatsRepository userActivityStatsRepository;
	private final BatchMetrics batchMetrics;

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		LocalDate today = LocalDate.now(SEOUL_ZONE);
		Instant periodDate = today.atStartOfDay(SEOUL_ZONE).toInstant();

		for (PeriodType periodType : PeriodType.values()) {
			Instant startAt = getStartAt(periodType, today);
			Instant endAt = getEndAt(today);

			Timestamp startTimestamp = Timestamp.from(startAt);
			Timestamp endTimestamp = Timestamp.from(endAt);

			Map<String, Double> reviewScoreMap = popularReviewsRepository
				.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.map(document -> document.getRankings()
					.stream()
					.collect(Collectors.toMap(
						PopularReviewsDocument.Ranking::getReviewId,
						PopularReviewsDocument.Ranking::getScore
					))
				)
				.orElse(Map.of());

			List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"""
				SELECT
					u.id AS user_id,
					u.nickname,
					COUNT(DISTINCT rl.id) AS like_count,
					COUNT(DISTINCT c.id) AS comment_count,
					COALESCE(
						(
							SELECT STRING_AGG(r.id::text, ',')
							FROM reviews r
							WHERE r.user_id = u.id
								AND r.created_at >= ?
								AND r.created_at < ?
						),
						''
					) AS review_ids
				FROM users u
				LEFT JOIN review_likes rl
					ON rl.user_id = u.id
					AND rl.created_at >= ?
					AND rl.created_at < ?
				LEFT JOIN comments c
					ON c.user_id = u.id
					AND c.created_at >= ?
					AND c.created_at < ?
				WHERE u.deleted_at IS NULL
				GROUP BY u.id, u.nickname
				""",
				startTimestamp,
				endTimestamp,
				startTimestamp,
				endTimestamp,
				startTimestamp,
				endTimestamp
			);

			List<PowerUsersDocument.Ranking> rankings = new ArrayList<>();

			for (Map<String, Object> row : rows) {
				long likeCount =
					((Number) row.get("like_count")).longValue();

				long commentCount =
					((Number) row.get("comment_count")).longValue();

				String reviewIds = row.get("review_ids").toString();

				double reviewScore = 0;

				if (!reviewIds.isBlank()) {
					for (String reviewId : reviewIds.split(",")) {
						reviewScore += reviewScoreMap.getOrDefault(
							reviewId.trim(),
							0.0
						);
					}
				}

				double activityScore =
					reviewScore * 0.5
						+ likeCount * 0.2
						+ commentCount * 0.3;

				rankings.add(
					new PowerUsersDocument.Ranking(
						0,
						row.get("user_id").toString(),
						row.get("nickname").toString(),
						activityScore,
						reviewScore,
						(int) likeCount,
						(int) commentCount
					)
				);
			}

			rankings.removeIf(
				ranking -> ranking.getActivityScore() <= 0
			);

			rankings.sort(
				(first, second) -> Double.compare(
					second.getActivityScore(),
					first.getActivityScore()
				)
			);

			for (int i = 0; i < rankings.size(); i++) {
				PowerUsersDocument.Ranking ranking = rankings.get(i);

				rankings.set(
					i,
					new PowerUsersDocument.Ranking(
						i + 1,
						ranking.getUserId(),
						ranking.getNickname(),
						ranking.getActivityScore(),
						ranking.getReviewScore(),
						ranking.getLikeCount(),
						ranking.getCommentCount()
					)
				);
			}

			Instant now = Instant.now();
			int updatedActivityStatsCount = 0;

			if (periodType == PeriodType.DAILY) {
				updatedActivityStatsCount = updateDailyPowerRanks(
					rankings,
					periodDate,
					now
				);
			}

			List<PowerUsersDocument.Ranking> top10 = rankings
				.stream()
				.limit(10)
				.toList();

			powerUsersRepository
				.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					document -> {
						document.update(top10, now);
						powerUsersRepository.save(document);
					},
					() -> powerUsersRepository.save(
						PowerUsersDocument.create(
							periodType,
							periodDate,
							top10,
							now
						)
					)
				);

			batchMetrics.recordProcessedItems(
				"powerUser",
				periodType.name(),
				top10.size()
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
				today.minusWeeks(1)
					.atStartOfDay(SEOUL_ZONE)
					.toInstant();

			case MONTHLY ->
				today.minusMonths(1)
					.atStartOfDay(SEOUL_ZONE)
					.toInstant();

			case ALL_TIME -> Instant.EPOCH;
		};
	}

	private Instant getEndAt(LocalDate today) {
		return today.atStartOfDay(SEOUL_ZONE).toInstant();
	}

	private int updateDailyPowerRanks(
		List<PowerUsersDocument.Ranking> rankings,
		Instant activityDate,
		Instant now
	) {
		List<UserActivityStatsDocument> documents =
			new ArrayList<>();

		for (PowerUsersDocument.Ranking ranking : rankings) {
			userActivityStatsRepository
				.findByUserIdAndActivityDate(
					ranking.getUserId(),
					activityDate
				)
				.ifPresent(document -> {
					document.updateDailyPowerRank(
						ranking.getRank(),
						now
					);

					documents.add(document);
				});
		}

		if (!documents.isEmpty()) {
			userActivityStatsRepository.saveAll(documents);
		}

		return documents.size();
	}
}