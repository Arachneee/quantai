package com.quantai.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface RealtimeOrderbookRepository : ReactiveMongoRepository<RealtimeOrderbook, String> {
    /**
     * 특정 종목의 실시간 호가 데이터를 시간순으로 조회
     */
    fun findByStockCodeOrderByTimestampDesc(stockCode: String): Flux<RealtimeOrderbook>

    /**
     * 특정 종목의 특정 시간 이후 실시간 호가 데이터 조회
     */
    fun findByStockCodeAndTimestampAfterOrderByTimestampDesc(
        stockCode: String,
        timestamp: LocalDateTime,
    ): Flux<RealtimeOrderbook>

    /**
     * 특정 종목의 최신 실시간 호가 데이터 조회
     */
    fun findFirstByStockCodeOrderByTimestampDesc(stockCode: String): Mono<RealtimeOrderbook>

    /**
     * 특정 시간 범위의 실시간 호가 데이터 조회
     */
    fun findByTimestampBetweenOrderByTimestampDesc(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Flux<RealtimeOrderbook>

    /**
     * 특정 종목의 특정 시간 범위 실시간 호가 데이터 조회
     */
    fun findByStockCodeAndTimestampBetweenOrderByTimestampDesc(
        stockCode: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Flux<RealtimeOrderbook>

    /**
     * 특정 시간 이전의 실시간 호가 데이터 삭제
     * @param timestamp 기준 시간
     * @return 삭제된 데이터 수
     */
    fun deleteByTimestampBefore(timestamp: LocalDateTime): Mono<Long>
}
