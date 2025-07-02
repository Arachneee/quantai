package com.quantai.batch

import com.quantai.log.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    private val stockDailyDataCollectionJob: Job,
    private val stockMinuteDataCollectionJob: Job,
    private val stockTodayDataCollectionJob: Job,
) {
    private val logger = logger()

    @Scheduled(cron = "0 0 6 * * *")
    fun runPreviousStockDailyDataCollectionJob() {
        runBatchJob(stockDailyDataCollectionJob, "과거 주식 일봉 데이터 수집")
    }

    @Scheduled(cron = "0 0 5 * * MON-FRI")
    fun runPreviousStockMinuteDataCollectionJob() {
        runBatchJob(stockMinuteDataCollectionJob, "과거 주식 분봉 데이터 수집")
    }

    @Scheduled(cron = "0 0 20 * * MON-FRI")
    fun runFinalTodayDataCollectionJob() {
        runBatchJob(stockTodayDataCollectionJob, "마감 주식 데이터 수집")
    }

    private fun runBatchJob(
        job: Job,
        description: String,
    ) {
        val jobParameters =
            JobParametersBuilder()
                .addString("uuid", UUID.randomUUID().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters()

        logger.info("$description 배치 작업 시작: ${LocalDateTime.now()}")
        jobLauncher.run(job, jobParameters)
    }
}
