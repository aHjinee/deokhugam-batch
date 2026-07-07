package com.sbproject.deokhugam.job.poweruser;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PowerUserJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Step powerUserStep(PowerUserTasklet tasklet) {
		return new StepBuilder("powerUserStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(tasklet)
			.build();
	}
}
