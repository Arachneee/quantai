package com.quantai.service

import com.quantai.domain.RealtimeExecution
import com.quantai.domain.RealtimeExecutionRepository
import com.quantai.domain.RealtimeOrderbook
import com.quantai.domain.RealtimeOrderbookRepository
import com.quantai.log.errorLog
import com.quantai.log.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class RealtimeDataStorageService(
    private val realtimeExecutionRepository: RealtimeExecutionRepository,
    private val realtimeOrderbookRepository: RealtimeOrderbookRepository,
) {
    private val logger = logger()

    // 배치 저장을 위한 버퍼
    private val executionBuffer = ConcurrentLinkedQueue<RealtimeExecution>()
    private val orderbookBuffer = ConcurrentLinkedQueue<RealtimeOrderbook>()

    /**
     * 실시간 데이터 비동기 저장 (타입에 따라 자동 분기)
     * 버퍼에 데이터를 추가하는 방식으로 변경
     */
    fun saveRealtimeDataAsync(data: Any): Mono<Void> =
        Mono
            .fromCallable {
                addToBuffer(data)
            }.subscribeOn(Schedulers.boundedElastic())
            .then()

    /**
     * 배치 저장을 위해 버퍼에 데이터 추가
     */
    private fun addToBuffer(data: Any) {
        when (data) {
            is RealtimeExecution -> {
                executionBuffer.offer(data)
                logger.debug("실시간 체결가 데이터 버퍼에 추가: ${data.stockCode}, 버퍼 크기: ${executionBuffer.size}")
            }

            is RealtimeOrderbook -> {
                orderbookBuffer.offer(data)
                logger.debug("실시간 호가 데이터 버퍼에 추가: ${data.stockCode}, 버퍼 크기: ${orderbookBuffer.size}")
            }

            else -> {
                logger.warn("알 수 없는 데이터 타입: ${data::class.simpleName}")
            }
        }
    }

    /**
     * 실시간 데이터 버퍼를 주기적으로 비우고 배치 저장합니다.
     * 10초마다 실행됩니다.
     */
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    fun scheduleBatchStorage() {
        val status = getBufferStatus()

        if (status["executionBufferSize"] == 0 && status["orderbookBufferSize"] == 0) {
            logger.debug("버퍼가 비어있어 배치 저장을 건너뜁니다.")
            return
        }

        logger.info("실시간 데이터 배치 저장 작업 시작 - 체결: ${status["executionBufferSize"]}건, 호가: ${status["orderbookBufferSize"]}건")

        flushBuffers()
            .subscribe(
                { logger.info("실시간 데이터 배치 저장 작업 완료") },
                { error ->
                    logger.error("실시간 데이터 배치 저장 작업 중 오류 발생: ${error.message}", error)
                },
            )
    }

    /**
     * 버퍼 상태 정보 조회
     */
    private fun getBufferStatus(): Map<String, Int> =
        mapOf(
            "executionBufferSize" to executionBuffer.size,
            "orderbookBufferSize" to orderbookBuffer.size,
        )

    /**
     * 버퍼의 데이터를 배치로 저장
     */
    private fun flushBuffers(): Mono<Void> {
        val executionList = mutableListOf<RealtimeExecution>()
        val orderbookList = mutableListOf<RealtimeOrderbook>()

        // 버퍼에서 데이터 추출
        while (executionBuffer.isNotEmpty()) {
            executionBuffer.poll()?.let { executionList.add(it) }
        }

        while (orderbookBuffer.isNotEmpty()) {
            orderbookBuffer.poll()?.let { orderbookList.add(it) }
        }

        val executionSave =
            if (executionList.isNotEmpty()) {
                realtimeExecutionRepository
                    .saveAll(executionList)
                    .doOnComplete {
                        logger.info("실시간 체결가 데이터 배치 저장 완료: ${executionList.size}건")
                    }.doOnError { error ->
                        logger.errorLog(error) { "실시간 체결가 데이터 배치 저장 중 오류 발생: ${executionList.size}건" }
                    }.then()
            } else {
                Mono.empty<Void>()
            }

        val orderbookSave =
            if (orderbookList.isNotEmpty()) {
                realtimeOrderbookRepository
                    .saveAll(orderbookList)
                    .doOnComplete {
                        logger.info("실시간 호가 데이터 배치 저장 완료: ${orderbookList.size}건")
                    }.doOnError { error ->
                        logger.errorLog(error) { "실시간 호가 데이터 배치 저장 중 오류 발생: ${orderbookList.size}건" }
                    }.then()
            } else {
                Mono.empty<Void>()
            }

        return executionSave.then(orderbookSave)
    }

    /**
     * 일주일 이상 된 실시간 데이터를 매일 자정에 삭제합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    fun cleanupOldData() {
        // 1주일 전 날짜 계산
        val oneWeekAgo = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(7)

        logger.info("일주일 이상 된 실시간 데이터 삭제 작업 시작 (기준일: $oneWeekAgo)")

        // 체결 데이터 삭제
        realtimeExecutionRepository
            .deleteByTimestampBefore(oneWeekAgo)
            .doOnSuccess { count ->
                logger.info("일주일 이상 된 체결 데이터 $count 건 삭제 완료")
            }.doOnError { error ->
                logger.errorLog(error) { "체결 데이터 삭제 중 오류 발생" }
            }.subscribe()

        // 호가 데이터 삭제
        realtimeOrderbookRepository
            .deleteByTimestampBefore(oneWeekAgo)
            .doOnSuccess { count ->
                logger.info("일주일 이상 된 호가 데이터 $count 건 삭제 완료")
            }.doOnError { error ->
                logger.errorLog(error) { "호가 데이터 삭제 중 오류 발생" }
            }.subscribe()
    }
}
