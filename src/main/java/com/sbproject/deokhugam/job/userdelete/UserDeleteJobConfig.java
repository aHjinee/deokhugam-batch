package com.sbproject.deokhugam.job.userdelete;

import com.sbproject.deokhugam.domain.user.service.UserService;
import com.sbproject.deokhugam.monitoring.BatchMetrics;
import com.sbproject.deokhugam.monitoring.BatchMetricsJobExecutionListener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class UserDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserService userService;
	private final BatchMetrics batchMetrics;
	private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;


	@Bean(name = "userDeleteJob")
    public Job userDeleteJob() {
        return new JobBuilder("userDeleteJob", jobRepository)
				.listener(batchMetricsJobExecutionListener)
				.start(userDeleteStep())
                .build();
    }

    @Bean
    public Step userDeleteStep() {
        return new StepBuilder("userDeleteStep", jobRepository)
                .tasklet(userDeleteTasklet(), transactionManager)
                .build();
    }

    @Bean
    public UserDeleteTasklet userDeleteTasklet() {
        return new UserDeleteTasklet(userService, batchMetrics);
    }
}