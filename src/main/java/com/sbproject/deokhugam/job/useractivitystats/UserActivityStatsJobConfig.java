package com.sbproject.deokhugam.job.useractivitystats;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.sbproject.deokhugam.monitoring.BatchMetricsJobExecutionListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class UserActivityStatsJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final UserActivityStatsTasklet userActivityStatsTasklet;
	private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

	@Bean
	public Step userActivityStatsStep() {
		return new StepBuilder("userActivityStatsStep", jobRepository)
			.tasklet(userActivityStatsTasklet, transactionManager)
			.build();
	}

	@Bean
	public Job userActivityStatsJob() {
		return new JobBuilder("userActivityStatsJob", jobRepository)
			.listener(batchMetricsJobExecutionListener)
			.start(userActivityStatsStep())
			.build();
	}
}