package com.sbproject.deokhugam.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationDeleteJob;
	private final Job rankingJob;

    public BatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier("notificationDeleteJob") Job notificationDeleteJob,
			@Qualifier("rankingJob") Job rankingJob) {

        this.jobLauncher = jobLauncher;
        this.notificationDeleteJob = notificationDeleteJob;
		this.rankingJob = rankingJob;

    }

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    //@Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    public void runNotificationDeleteJob() {

        try {

            JobParameters parameters =
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();

            jobLauncher.run(notificationDeleteJob, parameters);

            log.info("NotificationDeleteJob completed.");

        } catch (Exception e) {

            log.error("NotificationDeleteJob failed.", e);
        }
    }

	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
	public void runRankingJob() {
		try {
			JobParameters parameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();
			jobLauncher.run(rankingJob, parameters);
			log.info("RankingJob completed.");
		} catch (Exception e) {
			log.error("RankingJob failed.", e);
		}
	}
}
