package com.sbproject.deokhugam.job.poweruser;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.document.PowerUsersDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularReviewsRepository;
import com.sbproject.deokhugam.domain.dashboard.repository.PowerUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserTasklet implements Tasklet, StepExecutionListener {

	private final JdbcTemplate jdbcTemplate;
	private final PowerUsersRepository powerUsersRepository;
	private final PopularReviewsRepository popularReviewsRepository;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("[PowerUserTasklet] beforeStep");
	}

	@Override
	public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

		for (PeriodType periodType : PeriodType.values()) {
			Instant startAt = getStartAt(periodType, today);
			Instant endAt = today.plusDays(1)
				.atStartOfDay(ZoneId.of("Asia/Seoul"))
				.toInstant();

			Timestamp startTimestamp = Timestamp.from(startAt);
			Timestamp endTimestamp = Timestamp.from(endAt);

			// MongoDB에서 해당 기간 인기 리뷰 점수 Map 만들기 (reviewId -> score)
			Map<String, Double> reviewScoreMap = popularReviewsRepository
				.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.map(doc -> doc.getRankings().stream()
					.collect(Collectors.toMap(
						PopularReviewsDocument.Ranking::getReviewId,
						PopularReviewsDocument.Ranking::getScore
					))
				)
				.orElse(Map.of());

			// PostgreSQL에서 유저별 좋아요/댓글 집계
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"""
				SELECT
					u.id AS user_id,
					u.nickname,
					COUNT(DISTINCT rl.id) AS like_count,
					COUNT(DISTINCT c.id) AS comment_count,
					COALESCE(
						(SELECT STRING_AGG(r.id::text, ',')
						 FROM reviews r
						 WHERE r.user_id = u.id
						 AND r.created_at >= ? AND r.created_at < ?), ''
					) AS review_ids
				FROM users u
				LEFT JOIN review_likes rl ON rl.user_id = u.id
					AND rl.created_at >= ? AND rl.created_at < ?
				LEFT JOIN comments c ON c.user_id = u.id
					AND c.created_at >= ? AND c.created_at < ?
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

			// 점수 계산
			List<PowerUsersDocument.Ranking> rankings = new ArrayList<>();
			for (Map<String, Object> row : rows) {
				long likeCount = ((Number) row.get("like_count")).longValue();
				long commentCount = ((Number) row.get("comment_count")).longValue();
				String reviewIds = row.get("review_ids").toString();

				// 해당 유저의 리뷰 점수 합산
				double reviewScore = 0;
				if (!reviewIds.isBlank()) {
					for (String reviewId : reviewIds.split(",")) {
						reviewScore += reviewScoreMap.getOrDefault(reviewId.trim(), 0.0);
					}
				}

				double activityScore = reviewScore * 0.5 + likeCount * 0.2 + commentCount * 0.3;
				rankings.add(new PowerUsersDocument.Ranking(
					0, // 순위는 나중에
					row.get("user_id").toString(),
					row.get("nickname").toString(),
					activityScore,
					reviewScore,
					(int) likeCount,
					(int) commentCount
				));
			}

			// 점수 내림차순 정렬 후 순위 부여
			rankings.sort((a, b) -> Double.compare(b.getActivityScore(), a.getActivityScore()));
			for (int i = 0; i < rankings.size(); i++) {
				rankings.set(i, new PowerUsersDocument.Ranking(
					i + 1,
					rankings.get(i).getUserId(),
					rankings.get(i).getNickname(),
					rankings.get(i).getActivityScore(),
					rankings.get(i).getReviewScore(),
					rankings.get(i).getLikeCount(),
					rankings.get(i).getCommentCount()
				));
			}

			// 상위 10개만
			List<PowerUsersDocument.Ranking> top10 = rankings.stream().limit(10).toList();

			Instant now = Instant.now();
			powerUsersRepository.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					doc -> {
						doc.update(top10, now);
						powerUsersRepository.save(doc);
					},
					() -> powerUsersRepository.save(
						PowerUsersDocument.create(periodType, today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(), top10, now)
					)
				);

			log.info("[PowerUserTasklet] {} 완료, {}건", periodType, top10.size());
		}

		return RepeatStatus.FINISHED;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("[PowerUserTasklet] afterStep");
		return ExitStatus.COMPLETED;
	}

	private Instant getStartAt(PeriodType periodType, LocalDate today) {
		ZoneId zone = ZoneId.of("Asia/Seoul");
		return switch (periodType) {
			case DAILY -> today.atStartOfDay(zone).toInstant();
			case WEEKLY -> today.minusWeeks(1).atStartOfDay(zone).toInstant();
			case MONTHLY -> today.minusMonths(1).atStartOfDay(zone).toInstant();
			case ALL_TIME -> Instant.EPOCH;
		};
	}
}