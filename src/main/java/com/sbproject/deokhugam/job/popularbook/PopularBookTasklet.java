package com.sbproject.deokhugam.job.popularbook;

import com.sbproject.deokhugam.domain.dashboard.document.PopularBooksDocument;
import com.sbproject.deokhugam.domain.dashboard.entity.PeriodType;
import com.sbproject.deokhugam.domain.dashboard.repository.PopularBooksRepository;
import com.sbproject.deokhugam.monitoring.BatchMetrics;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class PopularBookTasklet implements Tasklet {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final JdbcTemplate jdbcTemplate;
	private final PopularBooksRepository popularBooksRepository;
	private final BatchMetrics batchMetrics;

	private final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(1))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		LocalDate today = LocalDate.now(SEOUL_ZONE);
		Map<String, Optional<String>> validatedUrlCache = new HashMap<>();

		for (PeriodType periodType : PeriodType.values()) {
			Instant startAt = getStartAt(periodType, today);
			Instant endAt = today.plusDays(1)
				.atStartOfDay(SEOUL_ZONE)
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
				HAVING (
					COUNT(DISTINCT r.id) * 0.4
					+ COALESCE(AVG(r.rating), 0) * 0.6
				) > 0
				ORDER BY (
					COUNT(DISTINCT r.id) * 0.4
					+ COALESCE(AVG(r.rating), 0) * 0.6
				) DESC
				LIMIT 10
				""",
				startTimestamp,
				endTimestamp
			);

			List<PopularBooksDocument.Ranking> rankings = new ArrayList<>();

			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> row = rows.get(i);

				long reviewCount =
					((Number) row.get("review_count")).longValue();

				double avgRating =
					((Number) row.get("avg_rating")).doubleValue();

				double score =
					reviewCount * 0.4 + avgRating * 0.6;

				String rawThumbnailUrl =
					row.get("thumbnail_url") != null
						? row.get("thumbnail_url").toString()
						: null;

				String thumbnailUrl = rawThumbnailUrl == null
					? null
					: validatedUrlCache
					.computeIfAbsent(
						rawThumbnailUrl,
						url -> Optional.ofNullable(validateThumbnailUrl(url))
					)
					.orElse(null);

				rankings.add(new PopularBooksDocument.Ranking(
					i + 1,
					row.get("book_id").toString(),
					row.get("title").toString(),
					row.get("author").toString(),
					thumbnailUrl,
					score,
					(int) reviewCount,
					avgRating
				));
			}

			Instant now = Instant.now();
			Instant periodDate =
				today.atStartOfDay(SEOUL_ZONE).toInstant();

			popularBooksRepository
				.findTopByPeriodTypeOrderByPeriodDateDesc(periodType)
				.ifPresentOrElse(
					document -> {
						document.update(rankings, now);
						popularBooksRepository.save(document);
					},
					() -> popularBooksRepository.save(
						PopularBooksDocument.create(
							periodType,
							periodDate,
							rankings,
							now
						)
					)
				);

			batchMetrics.recordProcessedItems(
				"popularBook",
				periodType.name(),
				rankings.size()
			);
		}

		return RepeatStatus.FINISHED;
	}

	private String validateThumbnailUrl(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}

		try {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(1))
				.method(
					"HEAD",
					HttpRequest.BodyPublishers.noBody()
				)
				.build();

			HttpResponse<Void> response =
				httpClient.send(
					request,
					HttpResponse.BodyHandlers.discarding()
				);

			int statusCode = response.statusCode();

			if (statusCode >= 200 && statusCode < 400) {
				return url;
			}


			return null;

		} catch (Exception e) {

			return null;
		}
	}

	private Instant getStartAt(
		PeriodType periodType,
		LocalDate today
	) {
		return switch (periodType) {
			case DAILY -> today.atStartOfDay(SEOUL_ZONE).toInstant();
			case WEEKLY ->
				today.minusWeeks(1).atStartOfDay(SEOUL_ZONE).toInstant();
			case MONTHLY ->
				today.minusMonths(1).atStartOfDay(SEOUL_ZONE).toInstant();
			case ALL_TIME -> Instant.EPOCH;
		};
	}
}