package com.sbproject.deokhugam.job.deletenotification;


import com.sbproject.deokhugam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationService notificationService;

    @Bean(name = "notificationDeleteJob")
    public Job notificationDeleteJob(Step notificationDeleteStep) {
        return new JobBuilder("notificationDeleteJob", jobRepository)
                .start(notificationDeleteStep)
                .build();
    }

    @Bean
    public Step notificationDeleteStep(NotificationDeleteTasklet tasklet) {
        return new StepBuilder("notificationDeleteStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(tasklet)
                .build();
    }

    @Bean
    public NotificationDeleteTasklet notificationDeleteTasklet() {
        return new NotificationDeleteTasklet(notificationService);
    }
}
