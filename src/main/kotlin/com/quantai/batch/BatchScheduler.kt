package com.quantai.batch

import com.quantai.log.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
@EnableScheduling
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    private val stockDailyDataCollectionJob: Job,
    private val stockMinuteDataCollectionJob: Job,
) {
    private val logger = logger()

    @Scheduled(cron = "0 0 22 * * *")
    fun runPreviousStockDailyDataCollectionJob() {
        val jobParameters =
            JobParametersBuilder()
                .addString("uuid", UUID.randomUUID().toString())
                .toJobParameters()

        logger.info("과거 주식 일봉 데이터 수집 배치 작업 시작: ${LocalDateTime.now()}")
        jobLauncher.run(stockDailyDataCollectionJob, jobParameters)
    }

    @Scheduled(cron = "0 0 23 * * *")
    fun runPreviousStockMinuteDataCollectionJob() {
        val jobParameters =
            JobParametersBuilder()
                .addString("uuid", UUID.randomUUID().toString())
                .toJobParameters()

        logger.info("과거 주식 분봉 데이터 수집 배치 작업 시작: ${LocalDateTime.now()}")
        jobLauncher.run(stockMinuteDataCollectionJob, jobParameters)
    }
}
