package com.quantai.domain

import com.quantai.api.dto.MinuteChartPrice
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 분봉 데이터 도메인 모델
 */
data class MinuteStockPrice(
    val date: LocalDate,
    val time: LocalTime,
    val stockCode: String,
    val currentPrice: BigDecimal,
    val openPrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val volume: Long,
    val tradingValue: Long
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")

        /**
         * API 응답의 MinuteChartPrice DTO를 도메인 객체로 변환
         */
        fun from(dto: MinuteChartPrice, stockCode: String): MinuteStockPrice {
            val date = LocalDate.parse(dto.date, dateFormatter)
            val time = LocalTime.parse(dto.time, timeFormatter)

            return MinuteStockPrice(
                date = date,
                time = time,
                stockCode = stockCode,
                currentPrice = BigDecimal(dto.currentPrice),
                openPrice = BigDecimal(dto.openPrice),
                highPrice = BigDecimal(dto.highPrice),
                lowPrice = BigDecimal(dto.lowPrice),
                volume = dto.volume.toLong(),
                tradingValue = dto.tradingValue.toLong()
            )
        }
    }
}
