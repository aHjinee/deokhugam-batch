package com.sbproject.deokhugam.job.userdelete;

import com.sbproject.deokhugam.domain.user.service.UserService;
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
public class UserDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    @Bean(name = "userDeleteJob")
    public Job userDeleteJob(Step userDeleteStep) {
        return new JobBuilder("userDeleteJob", jobRepository)
                .start(userDeleteStep)
                .build();
    }

    @Bean
    public Step userDeleteStep(UserDeleteTasklet tasklet) {
        return new StepBuilder("userDeleteStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(tasklet)
                .build();
    }

    @Bean
    public UserDeleteTasklet userDeleteTasklet() {
        return new UserDeleteTasklet(userService);
    }
}