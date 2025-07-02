package com.quantai.domain

import com.quantai.client.dto.DailyChartPrice
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
    val stockCode: String,
    @Indexed
    val date: LocalDate,
    val closePrice: Double,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Long,
    val tradingValue: Long,
    val fluctuationRate: Double,
    val upDownSign: String,
    val comparedToYesterday: Double,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private fun parseDate(dateStr: String): LocalDate {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            return LocalDate.parse(dateStr, formatter)
        }

        private fun parseDouble(value: String): Double =
            try {
                value.replace(",", "").toDouble()
            } catch (e: Exception) {
                0.0
            }

        private fun parseLong(value: String): Long =
            try {
                value.replace(",", "").toLong()
            } catch (e: Exception) {
                0L
            }

        fun from(
            dailyChartPrice: DailyChartPrice,
            stockCode: String,
        ): DailyStockPrice =
            DailyStockPrice(
                id = "${stockCode}_${dailyChartPrice.date}",
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
                comparedToYesterday = parseDouble(dailyChartPrice.comparedToYesterday),
            )
    }
}
