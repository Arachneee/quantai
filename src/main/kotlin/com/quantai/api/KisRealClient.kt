package com.quantai.api

import com.quantai.api.dto.MinuteChartResponse
import com.quantai.api.dto.StockPriceResponse
import com.quantai.config.KisRealClientProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@EnableConfigurationProperties(KisRealClientProperties::class)
class KisRealClient(
    private val webClientBuilder: WebClient.Builder,
    private val properties: KisRealClientProperties
) : KisClient(webClientBuilder, properties) {

    // 시간 포맷을 위한 formatter 추가
    private val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")

    /**
     * 국내 주식 일별 시세 조회 (차트 데이터)
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param startDate 시작일
     * @param endDate 종료일 (기본값: 오늘)
     * @param adjustedPrice 수정주가여부 (Y/N)
     */
    fun getDailyStockPrice(
        stockCode: String,
        startDate: LocalDate,
        endDate: LocalDate = LocalDate.now(),
        adjustedPrice: String = "N"
    ): Mono<StockPriceResponse> {
        return getAccessToken().flatMap { token ->
            webClient.get()
                .uri { builder ->
                    builder.path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "J") // 시장 구분
                        .queryParam("FID_INPUT_ISCD", stockCode) // 종목코드
                        .queryParam("FID_INPUT_DATE_1", startDate.format(dateFormatter)) // 시작일자
                        .queryParam("FID_INPUT_DATE_2", endDate.format(dateFormatter)) // 종료일자
                        .queryParam("FID_PERIOD_DIV_CODE", "D") // 기간분류코드(D:일, W:주, M:월)
                        .queryParam("FID_ORG_ADJ_PRC", adjustedPrice) // 수정주가 여부(Y/N)
                        .build()
                }
                .header("authorization", "Bearer $token")
                .header("appkey", properties.appKey)
                .header("appsecret", properties.appSecret)
                .header("tr_id", "FHKST03010100") // 일자별 주식 시세 TR ID
                .header("content-type", "application/json; charset=utf-8")
                .retrieve()
                .bodyToMono(StockPriceResponse::class.java)
                .doOnError { error ->
                    logger.error("일별 시세 조회 중 오류 발생: ${error.message}", error)
                }
        }
    }

    /**
     * 주식일별분봉조회 API (FHKST03010230)
     * 특정 일자의 분봉 데이터를 조회합니다.
     *
     * @param stockCode 종목 코드 (예: 삼성전자 "005930")
     * @param date 조회할 날짜 (YYYYMMDD 형식으로 변환됨)
     * @param time 조회할 시간 (HHMMSS 형식으로 변환됨, 기본값: 현재 시간)
     * @param includePastData 과거 데이터 포함 여부 (Y/N)
     * @param includeFakeTick 허봉 포함 여부 (Y/N)
     * @return 분봉 데이터 응답
     */
    fun getDailyMinuteChart(
        stockCode: String,
        date: LocalDate,
        time: LocalDateTime? = null,
        includePastData: String = "Y",
        includeFakeTick: String = "N"
    ): Mono<MinuteChartResponse> {
        // time이 null이면 현재 시간을 사용, 아니면 입력된 시간을 사용
        val formattedTime = time?.format(timeFormatter) ?: LocalDateTime.now().format(timeFormatter)

        return getAccessToken().flatMap { token ->
            webClient.get()
                .uri { builder ->
                    builder.path("/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "J") // 시장 구분 (J: KRX)
                        .queryParam("FID_INPUT_ISCD", stockCode) // 종목코드
                        .queryParam("FID_INPUT_DATE_1", date.format(dateFormatter)) // 조회 날짜
                        .queryParam("FID_INPUT_HOUR_1", formattedTime) // 조회 시간
                        .queryParam("FID_PW_DATA_INCU_YN", includePastData) // 과거 데이터 포함 여부
                        .queryParam("FID_FAKE_TICK_INCU_YN", includeFakeTick) // 허봉 포함 여부
                        .build()
                }
                .header("authorization", "Bearer $token")
                .header("appkey", properties.appKey)
                .header("appsecret", properties.appSecret)
                .header("tr_id", "FHKST03010230") // 주식일별분봉조회 TR ID
                .header("content-type", "application/json; charset=utf-8")
                .retrieve()
                .bodyToMono(MinuteChartResponse::class.java)
                .doOnError { error ->
                    logger.error("주식일별분봉조회 중 오류 발생: ${error.message}", error)
                }
        }
    }
}
