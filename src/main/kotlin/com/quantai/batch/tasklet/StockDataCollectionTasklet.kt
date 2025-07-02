package com.quantai.batch.tasklet

import com.quantai.domain.BatchExecutionHistory
import com.quantai.domain.BatchExecutionHistoryRepository
import com.quantai.log.errorLog
import com.quantai.log.logger
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import java.time.LocalDate
import java.time.LocalDateTime

abstract class StockDataCollectionTasklet(
    private val batchExecutionHistoryRepository: BatchExecutionHistoryRepository,
) : Tasklet {
    private val logger = logger()

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext,
    ): RepeatStatus {
        val jobParameters = chunkContext.stepContext.jobParameters
        val uuid = jobParameters["uuid"] as String
        val startDateTime = LocalDateTime.now()
        val jobName = getJobName()

        try {
            val collectedCount = collectAndSaveStockData()
            saveExecutionHistory(jobName, uuid, startDateTime, "COMPLETED", collectedCount)
            updateDateRange()
        } catch (e: Exception) {
            logger.errorLog(e) { "$jobName 실패: ${e.message}" }
            saveExecutionHistory(jobName, uuid, startDateTime, "FAILED", 0, e.message)
        }

        return RepeatStatus.FINISHED
    }

    protected abstract fun getJobName(): String

    protected abstract fun collectAndSaveStockData(): Int

    protected abstract fun updateDateRange()

    protected abstract fun getDataStartDate(): LocalDate

    protected abstract fun getDataEndDate(): LocalDate

    // 공통 히스토리 저장 로직
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
                    dataStartDate = getDataStartDate(),
                    dataEndDate = getDataEndDate(),
                    stockCount = stockCount,
                    errorMessage = errorMessage,
                ),
            ).block()
    }

    companion object {
        const val STOCK_COUNT = 200
    }
}
