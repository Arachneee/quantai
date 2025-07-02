package com.quantai.batch.tasklet

import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.service.StockDataCollectionService
import java.time.LocalDate
import java.time.LocalDateTime

class MinuteStockDataTasklet(
    private val stockDataCollectionService: StockDataCollectionService,
    batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) : StockDataCollectionTasklet(batchExecutionHistoryRepository) {
    override fun getJobName() = JOB_NAME

    override fun getDataStartDate(): LocalDate = batchEndDateTime.toLocalDate()

    override fun getDataEndDate(): LocalDate = batchEndDateTime.toLocalDate()

    override fun collectAndSaveStockData(): Int = stockDataCollectionService.collectMinutePrices(batchEndDateTime, STOCK_COUNT)

    override fun updateDateRange() {
        batchEndDateTime = batchEndDateTime.minusHours(2)
    }

    companion object {
        const val JOB_NAME = "stockMinuteDataCollectionJob"
        private var batchEndDateTime: LocalDateTime = LocalDateTime.now()
    }
}
