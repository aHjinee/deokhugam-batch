package com.sbproject.deokhugam.job.popularbook;

import com.sbproject.deokhugam.domain.dashboard.document.PopularBooksDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularBooksRepository;
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
public class PopularBookTasklet implements Tasklet, StepExecutionListener {

	private final JdbcTemplate jdbcTemplate;
	private final PopularBooksRepository popularBooksRepository;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("[PopularBookTasklet] beforeStep");
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
					b.id AS book_id,
					b.title,
					b.author,
					b.thumbnail_url,
					COUNT(DISTINCT r.id) AS review_count,
					COALESCE(AVG(r.rating), 0) AS avg_rating
				FROM books b
				LEFT JOIN reviews r ON r.book_id = b.id
					AND r.created_at >= ? AND r.created_at < ?
				WHERE b.deleted_at IS NULL
				GROUP BY b.id, b.title, b.author, b.thumbnail_url
				ORDER BY (COUNT(DISTINCT r.id) * 0.4 + COALESCE(AVG(r.rating), 0) * 0.6) DESC
				LIMIT 10
				""",

				startTimestamp,
				endTimestamp
			);

			List<PopularBooksDocument.Ranking> rankings = new ArrayList<>();
			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> row = rows.get(i);
				long reviewCount = ((Number) row.get("review_count")).longValue();
				double avgRating = ((Number) row.get("avg_rating")).doubleValue();
				double score = reviewCount * 0.4 + avgRating * 0.6;

				rankings.add(new PopularBooksDocument.Ranking(
					i + 1,
					row.get("book_id").toString(),
					row.get("title").toString(),
					row.get("author").toString(),
					row.get("thumbnail_url") != null ? row.get("thumbnail_url").toString() : null,
					score,
					(int) reviewCount,
					avgRating
				));
			}

			Instant now = Instant.now();
			popularBooksRepository.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					doc -> {
						doc.update(rankings, now);
						popularBooksRepository.save(doc);
					},
					() -> popularBooksRepository.save(
						PopularBooksDocument.create(periodType, today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(), rankings, now)
					)
				);

			log.info("[PopularBookTasklet] {} 완료, {}건", periodType, rankings.size());
		}

		return RepeatStatus.FINISHED;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("[PopularBookTasklet] afterStep");
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