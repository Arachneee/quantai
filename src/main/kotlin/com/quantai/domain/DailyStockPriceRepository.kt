package com.quantai.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Repository
interface DailyStockPriceRepository : ReactiveMongoRepository<DailyStockPrice, String> {

    // 특정 종목의 특정 기간 시세 데이터 조회
    fun findByStockCodeAndDateBetweenOrderByDateDesc(stockCode: String, startDate: LocalDate, endDate: LocalDate): Flux<DailyStockPrice>

    // 특정 날짜의 모든 종목 시세 데이터 조회
    fun findByDateOrderByStockCodeAsc(date: LocalDate): Flux<DailyStockPrice>

    fun existsByStockCodeAndDate(stockCode: String, date: LocalDate): Mono<Boolean>
}
