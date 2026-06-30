package com.sbproject.deokhugam.job.userdelete;

import com.sbproject.deokhugam.domain.user.service.UserService;
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
public class UserDeleteTasklet implements Tasklet, StepExecutionListener {

    private final UserService userService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[UserDeleteTasklet] beforeStep");
    }

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int deletedCount = userService.deleteExpiredUsers();
        log.info("[UserDeleteTasklet] deletedCount: {}", deletedCount);
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[UserDeleteTasklet] afterStep");
        return ExitStatus.COMPLETED;
    }
}