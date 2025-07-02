package com.quantai.service

import com.quantai.domain.DailyStockPriceRepository
import com.quantai.domain.MinuteStockPriceRepository
import com.quantai.log.errorLog
import com.quantai.log.logger
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StockDataCollectionService(
    private val stockService: StockService,
    private val dailyStockPriceRepository: DailyStockPriceRepository,
    private val minuteStockPriceRepository: MinuteStockPriceRepository,
    @Value("\${kis-client-real.maxRequestCountPerSec}")
    private val maxRequestCountPerSec: Int = 20,
) {
    private val logger = logger()

    fun getStockMarketCap(totalCount: Int): Flux<StockMarketCapDto> {
        val totalPages = (totalCount + maxRequestCountPerSec - 1) / maxRequestCountPerSec
        return Flux
            .range(0, totalPages)
            .flatMap { page ->
                val remainStocks = totalCount - maxRequestCountPerSec * page
                val limit = remainStocks.coerceAtMost(maxRequestCountPerSec)
                stockService.getMarketCapTop(page, limit)
            }
    }

    fun collectDailyPrices(
        startDate: LocalDate,
        endDate: LocalDate,
        stockCount: Int,
    ): Int =
        getStockMarketCap(stockCount)
            .flatMap { stock ->
                stockService
                    .getDailyStockPrice(stock.code, startDate, endDate)
                    .onErrorResume { e ->
                        logger.errorLog(e) { "Error collecting daily data for ${stock.code}: ${e.message}" }
                        Flux.empty()
                    }
            }.buffer(maxRequestCountPerSec * 7)
            .flatMap { dailyStockPriceRepository.saveAll(it) }
            .count()
            .block()
            ?.toInt() ?: 0

    fun collectMinutePrices(
        targetDateTime: LocalDateTime,
        stockCount: Int,
    ): Int =
        getStockMarketCap(stockCount)
            .flatMap { stock ->
                stockService
                    .getDailyMinuteChart(stock.code, targetDateTime.toLocalDate(), targetDateTime)
                    .onErrorResume { e ->
                        logger.errorLog(e) { "Error collecting minute data for ${stock.code}: ${e.message}" }
                        Flux.empty()
                    }
            }.buffer(maxRequestCountPerSec * 120)
            .flatMap { minuteStockPriceRepository.saveAll(it) }
            .count()
            .block()
            ?.toInt() ?: 0
}
