package com.sbproject.deokhugam.monitoring;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchMetricsJobExecutionListener
	implements JobExecutionListener {

	private final BatchMetrics batchMetrics;

	@Override
	public void afterJob(JobExecution jobExecution) {
		LocalDateTime startTime = jobExecution.getStartTime();
		LocalDateTime endTime = jobExecution.getEndTime();

		if (startTime == null || endTime == null) {
			return;
		}

		String jobName = jobExecution
			.getJobInstance()
			.getJobName();

		Duration duration = Duration.between(
			startTime,
			endTime
		);

		batchMetrics.recordJobExecution(
			jobName,
			jobExecution.getStatus(),
			duration
		);
	}
}