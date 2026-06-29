package com.sbproject.deokhugam.job.popularreview;

import com.sbproject.deokhugam.domain.dashboard.document.PopularReviewsDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularReviewsRepository;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularReviewTasklet implements Tasklet, StepExecutionListener {

	private final JdbcTemplate jdbcTemplate;
	private final PopularReviewsRepository popularReviewsRepository;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("[PopularReviewTasklet] beforeStep");
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
				GROUP BY r.id, r.user_id, r.book_id, u.nickname, b.title, b.thumbnail_url, r.content, r.rating
				ORDER BY (COUNT(DISTINCT rl.id) * 0.3 + COUNT(DISTINCT c.id) * 0.7) DESC
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
				long likeCount = ((Number) row.get("like_count")).longValue();
				long commentCount = ((Number) row.get("comment_count")).longValue();
				double score = likeCount * 0.3 + commentCount * 0.7;

				rankings.add(new PopularReviewsDocument.Ranking(
					i + 1,
					row.get("review_id").toString(),
					row.get("user_id").toString(),
					row.get("book_id").toString(),
					row.get("nickname").toString(),
					row.get("title").toString(),
					row.get("thumbnail_url") != null ? row.get("thumbnail_url").toString() : null,
					row.get("content").toString(),
					((Number) row.get("rating")).doubleValue(),
					score,
					(int) likeCount,
					(int) commentCount
				));
			}

			Instant now = Instant.now();
			popularReviewsRepository.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					doc -> {
						doc.update(rankings, now);
						popularReviewsRepository.save(doc);
					},
					() -> popularReviewsRepository.save(
						PopularReviewsDocument.create(periodType, today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(), rankings, now)
					)
				);

			log.info("[PopularReviewTasklet] {} 완료, {}건", periodType, rankings.size());
		}

		return RepeatStatus.FINISHED;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("[PopularReviewTasklet] afterStep");
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