package com.sbproject.deokhugam.monitoring;

import java.time.Duration;

import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchMetrics {

	private final MeterRegistry meterRegistry;

	/**
	 * Job 실행 횟수와 실행 시간을 기록한다.
	 * status 태그를 통해 COMPLETED, FAILED를 구분한다.
	 */
	public void recordJobExecution(
		String jobName,
		BatchStatus status,
		Duration duration
	) {
		Timer.builder("deokhugam.batch.job.execution")
			.description("덕후감 배치 Job 실행 횟수 및 실행 시간")
			.tag("job", jobName)
			.tag("status", status.name())
			.register(meterRegistry)
			.record(duration);
	}

	/**
	 * 각 Tasklet에서 실제로 저장한 데이터 수를 기록한다.
	 */
	public void recordProcessedItems(
		String taskName,
		String periodType,
		long count
	) {
		Counter counter = Counter
			.builder("deokhugam.batch.items.processed")
			.description("덕후감 배치 작업별 누적 처리 건수")
			.tag("task", taskName)
			.tag("period", periodType)
			.register(meterRegistry);

		if (count > 0) {
			counter.increment(count);
		}
	}
}