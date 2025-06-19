package com.quantai.api

import com.quantai.api.dto.StockPriceResponse
import com.quantai.api.dto.TokenRequest
import com.quantai.api.dto.TokenResponse
import com.quantai.config.KisClientProperties
import com.quantai.log.logger
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

@Component
@EnableConfigurationProperties(KisClientProperties::class)
class KisApiClient(
    private val webClientBuilder: WebClient.Builder,
    private val properties: KisClientProperties,
) {
    private val logger = logger()
    private val tokenRef = AtomicReference<String>(null)
    private val tokenExpired = AtomicReference<LocalDateTime>(null)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private lateinit var webClient: WebClient

    @PostConstruct
    fun initialize() {
        val baseUrl = if (properties.port > 0) {
            "${properties.host}:${properties.port}"
        } else {
            properties.host
        }

        webClient = webClientBuilder
            .baseUrl(baseUrl)
            .build()

        logger.info("KIS API Client initialized with baseUrl: $baseUrl")
    }

    fun getAccessToken(): Mono<String> {
        val existingToken = tokenRef.get()
        val existingTokenExpired = tokenExpired.get()
        if (existingToken != null && existingTokenExpired != null
            && LocalDateTime.now().isBefore(existingTokenExpired)
        ) {
            return Mono.just(existingToken)
        }

        return requestNewToken()
    }

    private fun requestNewToken(): Mono<String> {
        val request = TokenRequest(
            appKey = properties.appKey,
            appSecret = properties.appSecret
        )

        return webClient.post()
            .uri("/oauth2/tokenP")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .map { response ->
                val newToken = response.accessToken
                val accessTokenExpired = response.accessTokenExpired
                tokenRef.set(newToken)
                tokenExpired.set(accessTokenExpired)
                logger.info("KIS API 접근 토큰 발급 완료")
                newToken
            }
            .doOnError { error ->
                logger.error("토큰 발급 중 오류 발생: ${error.message}", error)
            }
    }

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
}
