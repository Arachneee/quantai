package com.quantai.batch

import com.quantai.domain.BatchExecutionHistory
import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.domain.DailyStockPrice
import com.quantai.domain.DailyStockPriceRepository
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Configuration
class StockDataCollectionBatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val stockService: StockService,
    private val dailyStockPriceRepository: DailyStockPriceRepository,
    private val batchExecutionHistoryRepository: BatchExecutionHistoryRepository

) {
    private val logger = logger()

    @Bean
    fun stockDataCollectionJob(): Job {
        return JobBuilder("stockDataCollectionJob", jobRepository)
            .start(collectStockDataStep())
            .build()
    }

    @Bean
    fun collectStockDataStep(): Step {
        return StepBuilder("collectStockDataStep", jobRepository)
            .tasklet(collectStockDataTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun collectStockDataTasklet(): Tasklet {
        return Tasklet { _, chunkContext ->
            val jobParameters = chunkContext.stepContext.jobParameters
            val uuid = jobParameters["uuid"] as String
            val startDateTime = LocalDateTime.now()

            try {
                val collectedCount = collectAndSaveStockData()
                saveExecutionHistory(uuid, startDateTime, "COMPLETED", collectedCount)
                updateDateRange()
            } catch (e: Exception) {
                logger.errorLog(e) { "Failed to collect stock data: ${e.message}" }
                saveExecutionHistory(uuid, startDateTime, "FAILED", 0, e.message)
            }

            RepeatStatus.FINISHED
        }
    }

    private fun collectAndSaveStockData(): Int {
        val totalPages = (STOCK_COUNT + BATCH_SIZE - 1) / BATCH_SIZE

        return Flux.range(0, totalPages)
            .delayElements(Duration.ofMillis(1500L))
            .flatMap { page -> fetchStocksByPage(page) }
            .doOnNext { logger.debug("Collecting stock data: ${it.code} - ${it.marketCap}") }
            .flatMap { fetchDailyPricesForStock(it) }
            .doOnNext { logger.debug("Fetched daily price for ${it.stockCode} on ${it.date}") }
            .filterWhen { isNewStockPrice(it) }
            .buffer(BATCH_SIZE)
            .flatMap { dailyStockPriceRepository.saveAll(it) }
            .doOnNext { logger.info("Saved stock data for ${it.stockCode} on ${it.date}") }
            .count()
            .block()?.toInt() ?: 0
    }

    private fun fetchStocksByPage(page: Int): Flux<StockMarketCapDto> {
        val remainStocks = STOCK_COUNT - BATCH_SIZE * page
        val limit = remainStocks.coerceAtMost(BATCH_SIZE)
        return stockService.getMarketCapTop(page, limit)
    }

    private fun isNewStockPrice(price: DailyStockPrice) =
        dailyStockPriceRepository.existsByStockCodeAndDate(price.stockCode, price.date).map(Boolean::not)

    private fun fetchDailyPricesForStock(stock: StockMarketCapDto): Flux<DailyStockPrice> {
        return stockService.getDailyStockPrice(stock.code, startDate, endDate)
            .onErrorResume { e ->
                logger.errorLog(e) { "Error collecting stock data for ${stock.code}: ${e.message}" }
                Flux.empty()
            }
    }

    private fun saveExecutionHistory(uuid: String, startTime: LocalDateTime, status: String, stockCount: Int, errorMessage: String? = null) {
        batchExecutionHistoryRepository.save(
            BatchExecutionHistory(
                uuid = uuid,
                jobName = "stockDataCollectionJob",
                status = status,
                startTime = startTime,
                endTime = LocalDateTime.now(),
                dataStartDate = startDate,
                dataEndDate = endDate,
                stockCount = stockCount,
                errorMessage = errorMessage
            )
        ).block()
    }

    private fun updateDateRange() {
        endDate = LocalDate.now().minusDays(1)
        startDate = endDate.minusWeeks(1)
    }

    companion object {
        private const val STOCK_COUNT = 2000
        private const val BATCH_SIZE = 20
        private var endDate: LocalDate = LocalDate.now().minusDays(1)
        private var startDate: LocalDate = endDate.minusWeeks(1)
    }
}
