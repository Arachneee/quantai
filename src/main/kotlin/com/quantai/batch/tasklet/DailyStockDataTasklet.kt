package com.quantai.batch.tasklet

import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.service.StockDataCollectionService
import java.time.LocalDate

class DailyStockDataTasklet(
    private val stockDataCollectionService: StockDataCollectionService,
    batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) : StockDataCollectionTasklet(batchExecutionHistoryRepository) {
    override fun getJobName() = JOB_NAME

    override fun getDataStartDate() = batchStartDate

    override fun getDataEndDate() = batchEndDate

    override fun collectAndSaveStockData(): Int = stockDataCollectionService.collectDailyPrices(batchStartDate, batchEndDate, STOCK_COUNT)

    override fun updateDateRange() {
        batchEndDate = LocalDate.now().minusDays(1)
        batchStartDate = batchEndDate.minusWeeks(1)
    }

    companion object {
        const val JOB_NAME = "stockDailyDataCollectionJob"
        private var batchEndDate: LocalDate = LocalDate.now().minusDays(1)
        private var batchStartDate: LocalDate = batchEndDate.minusWeeks(1)
    }
}
