package com.quantai.service

import com.quantai.api.KisMockClient
import com.quantai.api.KisRealClient
import com.quantai.api.dto.StockPriceResponse
import com.quantai.domain.StockCodeRepository
import com.quantai.domain.findMarketCapTop
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class StockService(
    private val kisMockClient: KisMockClient,
    private val kisRealClient: KisRealClient,
    private val stockCodeRepository: StockCodeRepository
) {

    /**
     * 종목코드, 시작일, 종료일을 받아 일별 주식 시세 데이터를 조회합니다.
     *
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param startDate 시작일
     * @param endDate 종료일 (기본값: 오늘)
     * @param adjustedPrice 수정주가여부 (기본값: "N")
     * @return 주식 시세 데이터
     */
    fun getDailyStockPrice(
        stockCode: String,
        startDate: LocalDate,
        endDate: LocalDate = LocalDate.now(),
        adjustedPrice: String = "N"
    ): Mono<StockPriceResponse> {
        return kisRealClient.getDailyStockPrice(stockCode, startDate, endDate, adjustedPrice)
    }

    /**
     * 시가총액 기준으로 상위 n개 종목을 조회합니다.
     *
     * @param limit 가져올 종목 수
     * @return 시가총액 내림차순으로 정렬된 상위 n개 종목
     */
    fun getMarketCapTop(page: Int, limit: Int): Flux<StockMarketCapDto> {
        val pageRequest = PageRequest.of(page, limit)
        return stockCodeRepository.findMarketCapTop(pageRequest)
            .map { StockMarketCapDto.from(it) }
    }
}
