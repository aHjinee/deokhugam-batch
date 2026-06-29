package com.sbproject.deokhugam.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RankingJobConfig {

	private final JobRepository jobRepository;

	@Bean
	public Job rankingJob(
		Step popularReviewStep,
		Step popularBookStep,
		Step powerUserStep
	) {
		return new JobBuilder("rankingJob", jobRepository)
			.start(popularReviewStep)
			.next(popularBookStep)
			.next(powerUserStep)
			.build();
	}
}