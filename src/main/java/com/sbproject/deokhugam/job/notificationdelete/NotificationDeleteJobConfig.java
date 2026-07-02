package com.sbproject.deokhugam.job.notificationdelete;


import com.sbproject.deokhugam.domain.notification.service.NotificationService;
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
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationService notificationService;
	private final BatchMetrics batchMetrics;
	private final BatchMetricsJobExecutionListener batchMetricsJobExecutionListener;

    @Bean(name = "notificationDeleteJob")
    public Job notificationDeleteJob(Step notificationDeleteStep) {
        return new JobBuilder("notificationDeleteJob", jobRepository)
				.listener(batchMetricsJobExecutionListener)
				.start(notificationDeleteStep)
                .build();
    }

    @Bean
    public Step notificationDeleteStep(NotificationDeleteTasklet tasklet) {
        return new StepBuilder("notificationDeleteStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public NotificationDeleteTasklet notificationDeleteTasklet() {
        return new NotificationDeleteTasklet(notificationService, batchMetrics);
    }
}
