package com.quantai.batch

import com.quantai.domain.BatchExecutionHistory
import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.domain.DailyStockPrice
import com.quantai.domain.DailyStockPriceRepository
import com.quantai.domain.MinuteStockPrice
import com.quantai.domain.MinuteStockPriceRepository
import com.quantai.log.errorLog
import com.quantai.log.logger
import com.quantai.service.StockService
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.time.LocalDateTime

@Configuration
class StockDataCollectionBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val stockService: StockService,
    private val dailyStockPriceRepository: DailyStockPriceRepository,
    private val minuteStockPriceRepository: MinuteStockPriceRepository,
    private val batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) {
    private val logger = logger()

    @Value("\${kis-client-real.maxRequestCountPerSec}")
    private var maxRequestCountPerSec: Int = 20

    @Bean
    fun stockDailyDataCollectionJob(): Job =
        JobBuilder(DAILY_JOB_NAME, jobRepository)
            .start(collectStockDailyDataStep())
            .build()

    private fun collectStockDailyDataStep(): Step =
        StepBuilder("collectStockDailyDataStep", jobRepository)
            .tasklet(collectStockDailyDataTasklet(), transactionManager)
            .build()

    private fun collectStockDailyDataTasklet(): Tasklet =
        Tasklet { _, chunkContext ->
            val jobParameters = chunkContext.stepContext.jobParameters
            val uuid = jobParameters["uuid"] as String
            val startDateTime = LocalDateTime.now()

            try {
                val collectedCount = collectAndSaveStockDailyData()
                saveExecutionHistory(DAILY_JOB_NAME, uuid, startDateTime, "COMPLETED", collectedCount)
                updateDateRange()
            } catch (e: Exception) {
                logger.errorLog(e) { "Failed to collect stock data: ${e.message}" }
                saveExecutionHistory(DAILY_JOB_NAME, uuid, startDateTime, "FAILED", 0, e.message)
            }

            RepeatStatus.FINISHED
        }

    private fun collectAndSaveStockDailyData(): Int =
        getStockMarketCap()
            .flatMap { fetchDailyPricesForStock(it) }
            .buffer(maxRequestCountPerSec * DAILY_GET_COUNT)
            .flatMap { dailyStockPriceRepository.saveAll(it) }
            .count()
            .block()
            ?.toInt() ?: 0

    private fun fetchDailyPricesForStock(stock: StockMarketCapDto): Flux<DailyStockPrice> =
        stockService
            .getDailyStockPrice(stock.code, dailyBatchStartDate, dailyBatchEndDate)
            .onErrorResume { e ->
                logger.errorLog(e) { "Error collecting stock daily data for ${stock.code}: ${e.message}" }
                Flux.empty()
            }

    @Bean
    fun stockMinuteDataCollectionJob(): Job =
        JobBuilder(MINUTE_JOB_NAME, jobRepository)
            .start(collectStockMinuteDataStep())
            .build()

    private fun collectStockMinuteDataStep(): Step =
        StepBuilder("collectStockDailyDataStep", jobRepository)
            .tasklet(collectStockMinuteDataTasklet(), transactionManager)
            .build()

    private fun collectStockMinuteDataTasklet(): Tasklet =
        Tasklet { _, chunkContext ->
            val jobParameters = chunkContext.stepContext.jobParameters
            val uuid = jobParameters["uuid"] as String
            val startDateTime = LocalDateTime.now()

            try {
                val collectedCount = collectAndSaveStockMinuteData()
                saveExecutionHistory(MINUTE_JOB_NAME, uuid, startDateTime, "COMPLETED", collectedCount)
                updateDateRange()
            } catch (e: Exception) {
                logger.errorLog(e) { "Failed to collect stock minute data: ${e.message}" }
                saveExecutionHistory(MINUTE_JOB_NAME, uuid, startDateTime, "FAILED", 0, e.message)
            }

            RepeatStatus.FINISHED
        }

    private fun collectAndSaveStockMinuteData(): Int =
        getStockMarketCap()
            .flatMap { fetchMinutePricesForStock(it) }
            .buffer(maxRequestCountPerSec * MINUTE_GET_COUNT)
            .flatMap { minuteStockPriceRepository.saveAll(it) }
            .count()
            .block()
            ?.toInt() ?: 0

    private fun fetchMinutePricesForStock(stock: StockMarketCapDto): Flux<MinuteStockPrice> =
        stockService
            .getDailyMinuteChart(
                stock.code,
                minuteBatchEndDateTime.toLocalDate(),
                minuteBatchEndDateTime,
            ).onErrorResume { e ->
                logger.errorLog(e) { "Error collecting stock data for ${stock.code}: ${e.message}" }
                Flux.empty()
            }

    private fun getStockMarketCap(): Flux<StockMarketCapDto> {
        val totalPages = (STOCK_TOTAL_COUNT + maxRequestCountPerSec - 1) / maxRequestCountPerSec

        return Flux
            .range(0, totalPages)
            .flatMap { page ->
                val remainStocks = STOCK_TOTAL_COUNT - maxRequestCountPerSec * page
                val limit = remainStocks.coerceAtMost(maxRequestCountPerSec)
                stockService.getMarketCapTop(page, limit)
            }
    }

    private fun saveExecutionHistory(
        jobName: String,
        uuid: String,
        startTime: LocalDateTime,
        status: String,
        stockCount: Int,
        errorMessage: String? = null,
    ) {
        batchExecutionHistoryRepository
            .save(
                BatchExecutionHistory(
                    uuid = uuid,
                    jobName = jobName,
                    status = status,
                    startTime = startTime,
                    endTime = LocalDateTime.now(),
                    dataStartDate = dailyBatchStartDate,
                    dataEndDate = dailyBatchEndDate,
                    stockCount = stockCount,
                    errorMessage = errorMessage,
                ),
            ).block()
    }

    private fun updateDateRange() {
        dailyBatchEndDate = LocalDate.now().minusDays(1)
        dailyBatchStartDate = dailyBatchEndDate.minusWeeks(1)
    }

    companion object {
        private const val DAILY_JOB_NAME = "stockDailyDataCollectionJob"
        private const val MINUTE_JOB_NAME = "stockMinuteDataCollectionJob"
        private const val STOCK_TOTAL_COUNT = 200
        private const val DAILY_GET_COUNT = 7
        private const val MINUTE_GET_COUNT = 120
        private var dailyBatchEndDate: LocalDate = LocalDate.now()
        private var dailyBatchStartDate: LocalDate = dailyBatchEndDate.minusWeeks(1)
        private var minuteBatchEndDateTime: LocalDateTime = LocalDateTime.now()
    }
}
