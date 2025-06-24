package com.quantai.service

import com.quantai.api.KisMockClient
import com.quantai.api.KisRealClient
import com.quantai.domain.DailyStockPrice
import com.quantai.domain.MinuteStockPrice
import com.quantai.domain.StockCodeRepository
import com.quantai.domain.findMarketCapTop
import com.quantai.log.logger
import com.quantai.service.dto.StockMarketCapDto
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StockService(
    private val kisMockClient: KisMockClient,
    private val kisRealClient: KisRealClient,
    private val stockCodeRepository: StockCodeRepository,
) {
    private val logger = logger()

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
    ): Flux<DailyStockPrice> {
        return kisRealClient.getDailyStockPrice(stockCode, startDate, endDate, adjustedPrice)
            .filter { it.resultCode == "0" }
            .flatMapMany { response ->
                Flux.fromIterable(response.priceList)
                    .map { DailyStockPrice.from(it, stockCode) }
            }
    }

    /**
     * 시가총액 기준으로 상위 n개 종목을 조회합니다.
     *
     * @param limit 가져올 종목 수
     * @return 시가총액 내림차순으로 정렬된 상위 n개 종목
     */
    fun getMarketCapTop(page: Int, limit: Int): Flux<StockMarketCapDto> {
        logger.info("Collecting $page $limit")
        val pageRequest = PageRequest.of(page, limit)
        return stockCodeRepository.findMarketCapTop(pageRequest)
            .map { StockMarketCapDto.from(it) }
    }

    /**
     * 특정 일자의 분봉 데이터를 조회합니다.
     *
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param date 조회할 날짜 (기본값: 오늘)
     * @param time 조회할 시간 (기본값: 현재 시간)
     * @param includePastData 과거 데이터 포함 여부 (기본값: "Y")
     * @param includeFakeTick 허봉 포함 여부 (기본값: "N")
     * @return 분봉 데이터
     */
    fun getDailyMinuteChart(
        stockCode: String,
        date: LocalDate = LocalDate.now(),
        time: LocalDateTime? = null,
        includePastData: String = "Y",
        includeFakeTick: String = "N"
    ): Flux<MinuteStockPrice> {
        logger.info("분봉 데이터 조회 요청 - 종목: $stockCode, 날짜: $date, 시간: $time")

        return kisRealClient.getDailyMinuteChart(stockCode, date, time, includePastData, includeFakeTick)
            .filter { it.resultCode == "0" }
            .flatMapMany { response ->
                logger.info("분봉 데이터 응답 성공 - 종목: $stockCode, 응답 코드: ${response.resultCode}")

                Flux.fromIterable(response.priceList.orEmpty())
                    .map { MinuteStockPrice.from(it, stockCode) }
            }
            .doOnError { error ->
                logger.error("분봉 데이터 조회 오류 - 종목: $stockCode, 오류: ${error.message}", error)
            }
    }
}
