package com.sbproject.deokhugam.job.notificationdelete;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.sbproject.deokhugam.domain.notification.service.NotificationService;
import com.sbproject.deokhugam.monitoring.BatchMetrics;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationDeleteTasklet implements Tasklet {

    private final NotificationService notificationService;
	private final BatchMetrics batchMetrics;


    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int deletedCount = notificationService.deleteExpiredConfirmedNotifications();
		batchMetrics.recordProcessedItems(
			"notificationDelete",
			"ALL",
			deletedCount
		);
        return RepeatStatus.FINISHED;
    }

}
