package com.quantai.domain

import com.quantai.api.dto.DailyChartPrice
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Document(collection = "daily_stock_prices")
@CompoundIndex(name = "stockCode_date_idx", def = "{'stockCode': 1, 'date': 1}", unique = true)
data class DailyStockPrice(
    @Id
    val id: String? = null,

    @Indexed
    val stockCode: String, // 종목 코드

    @Indexed
    val date: LocalDate, // 날짜

    val closePrice: Double, // 종가
    val openPrice: Double, // 시가
    val highPrice: Double, // 고가
    val lowPrice: Double, // 저가
    val volume: Long, // 거래량
    val tradingValue: Long, // 거래대금
    val fluctuationRate: Double, // 등락률
    val upDownSign: String, // 전일 대비 부호
    val comparedToYesterday: Double, // 전일 대비

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private fun parseDate(dateStr: String): LocalDate {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            return LocalDate.parse(dateStr, formatter)
        }

        private fun parseDouble(value: String): Double {
            return try {
                value.replace(",", "").toDouble()
            } catch (e: Exception) {
                0.0
            }
        }

        private fun parseLong(value: String): Long {
            return try {
                value.replace(",", "").toLong()
            } catch (e: Exception) {
                0L
            }
        }

        fun from(dailyChartPrice: DailyChartPrice, stockCode: String): DailyStockPrice {
            return DailyStockPrice(
                stockCode = stockCode,
                date = parseDate(dailyChartPrice.date),
                closePrice = parseDouble(dailyChartPrice.closePrice),
                openPrice = parseDouble(dailyChartPrice.openPrice),
                highPrice = parseDouble(dailyChartPrice.highPrice),
                lowPrice = parseDouble(dailyChartPrice.lowPrice),
                volume = parseLong(dailyChartPrice.volume),
                tradingValue = parseLong(dailyChartPrice.tradingValue),
                fluctuationRate = parseDouble(dailyChartPrice.fluctuationRate),
                upDownSign = dailyChartPrice.upDownSign,
                comparedToYesterday = parseDouble(dailyChartPrice.comparedToYesterday)
            )
        }
    }
}
