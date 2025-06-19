package com.quantai.controller

import com.quantai.api.dto.StockPriceResponse
import com.quantai.service.StockService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
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
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate? = LocalDate.now().minusWeeks(1),
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate? = LocalDate.now(),
        @RequestParam(required = false, defaultValue = "N") adjustedPrice: String
    ): Mono<StockPriceResponse> {
        return stockService.getDailyStockPrice(
            stockCode,
            startDate ?: LocalDate.now().minusWeeks(1),
            endDate ?: LocalDate.now(),
            adjustedPrice
        )
    }
}
