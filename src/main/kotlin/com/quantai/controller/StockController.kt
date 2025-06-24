package com.quantai.controller

import com.quantai.domain.DailyStockPrice
import com.quantai.domain.MinuteStockPrice
import com.quantai.service.StockService
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RestController
@RequestMapping("/api/stocks")
class StockController(
    private val stockService: StockService,
) {
    /**
     * 특정 종목의 일별 시세 데이터를 조회합니다.
     *
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param startDate 시작일 (기본값: 1주일 전)
     * @param endDate 종료일 (기본값: 오늘)
     * @param adjustedPrice 수정주가여부 (기본값: 'N')
     * @return 일별 시세 데이터
     */
    @GetMapping("/{stockCode}/daily-prices")
    fun getDailyStockPrices(
        @PathVariable stockCode: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate? =
            LocalDate
                .now()
                .minusWeeks(1),
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate? = LocalDate.now(),
        @RequestParam(required = false, defaultValue = "N") adjustedPrice: String,
    ): Flux<DailyStockPrice> =
        stockService.getDailyStockPrice(
            stockCode,
            startDate ?: LocalDate.now().minusWeeks(1),
            endDate ?: LocalDate.now(),
            adjustedPrice,
        )

    /**
     * 시가총액 기준으로 상위 n개 종목을 조회합니다.
     *
     * @param limit 가져올 종목 수 (기본값: 100)
     * @return 시가총액 내림차순으로 정렬된 상위 n개 종목 목록
     */
    @GetMapping("/market-cap/top")
    fun getMarketCapTop(
        @RequestParam(required = false, defaultValue = "100") limit: Int,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
    ): Flux<StockMarketCapDto> {
        val safeLimit =
            when {
                limit <= 0 -> 100 // 기본값
                limit > 5000 -> 5000 // 최대값
                else -> limit
            }
        val safePage = if (page < 0) 0 else page
        return stockService.getMarketCapTop(safePage, safeLimit)
    }

    /**
     * 특정 종목의 일별 분봉 데이터를 조회합니다.
     *
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param date 조회 날짜 (기본값: 오늘)
     * @param hour 시간(HH) 조회 시각의 시간 부분 (기본값: 현재 시간)
     * @param minute 분(mm) 조회 시각의 분 부분 (기본값: 현재 시간)
     * @param includePastData 과거 데이터 포함 여부 (Y/N, 기본값: Y)
     * @param includeFakeTick 허봉 포함 여부 (Y/N, 기본값: N)
     * @return 분봉 데이터
     */
    @GetMapping("/{stockCode}/minute-chart")
    fun getDailyMinuteChart(
        @PathVariable stockCode: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate? = LocalDate.now(),
        @RequestParam(required = false) hour: Int? = null,
        @RequestParam(required = false) minute: Int? = null,
        @RequestParam(required = false, defaultValue = "Y") includePastData: String,
        @RequestParam(required = false, defaultValue = "N") includeFakeTick: String,
    ): Flux<MinuteStockPrice> {
        // 현재 날짜 또는 사용자 지정 날짜 사용
        val queryDate = date ?: LocalDate.now()

        // 사용자가 시간과 분을 지정한 경우 해당 시간으로 LocalDateTime 생성
        val queryTime =
            if (hour != null && minute != null) {
                LocalDateTime.of(queryDate, LocalTime.of(hour, minute))
            } else {
                null // null이면 service 메서드에서 현재 시간 사용
            }

        return stockService.getDailyMinuteChart(stockCode, queryDate, queryTime, includePastData, includeFakeTick)
    }
}
