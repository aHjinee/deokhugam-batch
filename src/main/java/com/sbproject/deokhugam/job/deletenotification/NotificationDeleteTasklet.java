package com.sbproject.deokhugam.job.deletenotification;

import com.sbproject.deokhugam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class NotificationDeleteTasklet implements Tasklet, StepExecutionListener {

    private final NotificationService notificationService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[NotificationDeleteTasklet] beforeStep");
    }

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int deletedCount = notificationService.deleteExpiredConfirmedNotifications();
        log.info("[NotificationDeleteTasklet] deletedCount: {}", deletedCount);
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[NotificationDeleteTasklet] afterStep");
        return ExitStatus.COMPLETED;
    }
}
