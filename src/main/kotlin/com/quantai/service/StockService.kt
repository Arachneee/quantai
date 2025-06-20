package com.quantai.service

import com.quantai.api.KisMockClient
import com.quantai.api.KisRealClient
import com.quantai.api.dto.StockPriceResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class StockService(
    private val kisMockClient: KisMockClient,
    private val kisRealClient: KisRealClient
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
}
