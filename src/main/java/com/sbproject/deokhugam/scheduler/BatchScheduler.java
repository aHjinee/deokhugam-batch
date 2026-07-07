package com.sbproject.deokhugam.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationDeleteJob;
    private final Job rankingJob;
    private final Job userActivityStatsJob;
    private final Job userDeleteJob;

    public BatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier("notificationDeleteJob") Job notificationDeleteJob,
            @Qualifier("rankingJob") Job rankingJob,
            @Qualifier("userActivityStatsJob") Job userActivityStatsJob,
            @Qualifier("userDeleteJob") Job userDeleteJob) {

        this.jobLauncher = jobLauncher;
        this.notificationDeleteJob = notificationDeleteJob;
        this.rankingJob = rankingJob;
        this.userActivityStatsJob = userActivityStatsJob;
        this.userDeleteJob = userDeleteJob;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runNotificationDeleteJob() {
        runJob(notificationDeleteJob, "NotificationDeleteJob");
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void runRankingJob() {
        runJob(rankingJob, "RankingJob");
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void runUserActivityStatsJob() {
        runJob(userActivityStatsJob, "UserActivityStatsJob");
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void runUserDeleteJob() {
        runJob(userDeleteJob, "UserDeleteJob");
    }

    private void runJob(Job job, String jobName) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(job, parameters);
            log.info("{} completed.", jobName);

        } catch (Exception e) {
            log.error("{} failed.", jobName, e);
        }
    }
}