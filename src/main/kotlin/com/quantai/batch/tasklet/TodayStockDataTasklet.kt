package com.quantai.batch.tasklet

import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.service.StockDataCollectionService
import java.time.LocalDate
import java.time.LocalDateTime

class TodayStockDataTasklet(
    private val stockDataCollectionService: StockDataCollectionService,
    batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) : StockDataCollectionTasklet(batchExecutionHistoryRepository) {
    private val today = LocalDate.now()

    override fun getJobName() = JOB_NAME

    override fun getDataStartDate() = today

    override fun getDataEndDate() = today

    override fun collectAndSaveStockData(): Int {
        val dailyCount = stockDataCollectionService.collectDailyPrices(today, today, STOCK_COUNT)
        val minuteCount = stockDataCollectionService.collectMinutePrices(LocalDateTime.now(), STOCK_COUNT)
        return dailyCount + minuteCount
    }

    override fun updateDateRange() {
        // 당일 데이터는 날짜 업데이트가 필요 없음
    }

    companion object {
        const val JOB_NAME = "stockTodayDataCollectionJob"
    }
}
