package com.quantai.controller

import com.quantai.domain.DailyStockPrice
import com.quantai.service.StockService
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.LocalDate

@RestController
@RequestMapping("/api/stocks")
class StockController(private val stockService: StockService) {

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
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate? = LocalDate.now()
            .minusWeeks(1),
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate? = LocalDate.now(),
        @RequestParam(required = false, defaultValue = "N") adjustedPrice: String
    ): Flux<DailyStockPrice> {
        return stockService.getDailyStockPrice(
            stockCode,
            startDate ?: LocalDate.now().minusWeeks(1),
            endDate ?: LocalDate.now(),
            adjustedPrice
        )
    }

    /**
     * 시가총액 기준으로 상위 n개 종목을 조회합니다.
     *
     * @param limit 가져올 종목 수 (기본값: 100)
     * @return 시가총액 내림차순으로 정렬된 상위 n개 종목 목록
     */
    @GetMapping("/market-cap/top")
    fun getMarketCapTop(
        @RequestParam(required = false, defaultValue = "100") limit: Int,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0
    ): Flux<StockMarketCapDto> {
        val safeLimit = when {
            limit <= 0 -> 100 // 기본값
            limit > 5000 -> 5000 // 최대값
            else -> limit
        }
        val safePage = if (page < 0) 0 else page
        return stockService.getMarketCapTop(page, safeLimit)
    }
}
