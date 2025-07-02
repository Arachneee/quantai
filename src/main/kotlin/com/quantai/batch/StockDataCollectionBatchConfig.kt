package com.quantai.batch

import com.quantai.batch.tasklet.DailyStockDataTasklet
import com.quantai.batch.tasklet.MinuteStockDataTasklet
import com.quantai.batch.tasklet.TodayStockDataTasklet
import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.service.StockDataCollectionService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class StockDataCollectionBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val stockDataCollectionService: StockDataCollectionService,
    private val batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) {
    @Bean
    fun stockDailyDataCollectionJob(): Job =
        JobBuilder(DailyStockDataTasklet.JOB_NAME, jobRepository)
            .start(collectStockDailyDataStep())
            .build()

    @Bean
    fun stockMinuteDataCollectionJob(): Job =
        JobBuilder(MinuteStockDataTasklet.JOB_NAME, jobRepository)
            .start(collectStockMinuteDataStep())
            .build()

    @Bean
    fun stockTodayDataCollectionJob(): Job =
        JobBuilder(TodayStockDataTasklet.JOB_NAME, jobRepository)
            .start(collectStockTodayDataStep())
            .build()

    private fun collectStockDailyDataStep(): Step =
        StepBuilder("collectStockDailyDataStep", jobRepository)
            .tasklet(
                DailyStockDataTasklet(stockDataCollectionService, batchExecutionHistoryRepository),
                transactionManager,
            ).build()

    private fun collectStockMinuteDataStep(): Step =
        StepBuilder("collectStockMinuteDataStep", jobRepository)
            .tasklet(
                MinuteStockDataTasklet(stockDataCollectionService, batchExecutionHistoryRepository),
                transactionManager,
            ).build()

    private fun collectStockTodayDataStep(): Step =
        StepBuilder("collectStockTodayDataStep", jobRepository)
            .tasklet(
                TodayStockDataTasklet(stockDataCollectionService, batchExecutionHistoryRepository),
                transactionManager,
            ).build()
}
