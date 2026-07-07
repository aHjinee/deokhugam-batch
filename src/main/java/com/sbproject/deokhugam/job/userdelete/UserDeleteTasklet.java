package com.sbproject.deokhugam.job.userdelete;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.sbproject.deokhugam.domain.user.service.UserService;
import com.sbproject.deokhugam.monitoring.BatchMetrics;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserDeleteTasklet implements Tasklet {

    private final UserService userService;
	private final BatchMetrics batchMetrics;


    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int deletedCount = userService.deleteExpiredUsers();
		batchMetrics.recordProcessedItems(
			"userDelete",
			"ALL",
			deletedCount
		);
        return RepeatStatus.FINISHED;
    }

}