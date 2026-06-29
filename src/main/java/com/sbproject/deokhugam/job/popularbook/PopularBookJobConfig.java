package com.sbproject.deokhugam.job.popularbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class PopularBookJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Step popularBookStep(PopularBookTasklet tasklet) {
		return new StepBuilder("popularBookStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(tasklet)
			.build();
	}
}
