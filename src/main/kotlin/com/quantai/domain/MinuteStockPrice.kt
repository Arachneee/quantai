package com.quantai.domain

import com.quantai.api.dto.MinuteChartPrice
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 분봉 데이터 도메인 모델
 * MongoDB에 저장 가능한 Document 형태로 구현
 */
@Document(collection = "minute_stock_prices")
@CompoundIndexes(
    CompoundIndex(name = "stock_date_time_idx", def = "{'stockCode': 1, 'date': 1, 'time': 1}", unique = true),
)
data class MinuteStockPrice(
    @Id
    val id: String? = null,
    @Field("stock_code")
    val stockCode: String,
    @Field("date")
    val date: LocalDate,
    @Field("time")
    val time: LocalTime,
    @Field("current_price")
    val currentPrice: BigDecimal,
    @Field("open_price")
    val openPrice: BigDecimal,
    @Field("high_price")
    val highPrice: BigDecimal,
    @Field("low_price")
    val lowPrice: BigDecimal,
    @Field("volume")
    val volume: Long,
    @Field("trading_value")
    val tradingValue: Long,
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")

        /**
         * API 응답의 MinuteChartPrice DTO를 도메인 객체로 변환
         */
        fun from(
            dto: MinuteChartPrice,
            stockCode: String,
        ): MinuteStockPrice {
            val date = LocalDate.parse(dto.date, dateFormatter)
            val time = LocalTime.parse(dto.time, timeFormatter)

            // Document ID는 stockCode_date_time 형태로 생성 (고유성 보장)
            val id = "${stockCode}_${dto.date}_${dto.time}"

            return MinuteStockPrice(
                id = id,
                date = date,
                time = time,
                stockCode = stockCode,
                currentPrice = BigDecimal(dto.currentPrice),
                openPrice = BigDecimal(dto.openPrice),
                highPrice = BigDecimal(dto.highPrice),
                lowPrice = BigDecimal(dto.lowPrice),
                volume = dto.volume.toLong(),
                tradingValue = dto.tradingValue.toLong(),
            )
        }
    }
}
