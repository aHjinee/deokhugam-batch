package com.sbproject.deokhugam.job.ranking;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sbproject.deokhugam.monitoring.BatchMetricsJobExecutionListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RankingJobConfig {

	@Bean
	public Job rankingJob(
		JobRepository jobRepository,
		Step popularReviewStep,
		Step popularBookStep,
		Step powerUserStep,
		BatchMetricsJobExecutionListener metricsListener
	) {
		return new JobBuilder("rankingJob", jobRepository)
			.listener(metricsListener)
			.start(popularReviewStep)
			.next(popularBookStep)
			.next(powerUserStep)
			.build();
	}
}