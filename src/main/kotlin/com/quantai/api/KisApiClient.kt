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
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private lateinit var webClient: WebClient

    @PostConstruct
    fun initialize() {
        webClient = webClientBuilder
            .baseUrl(properties.host)
            .build()

        logger.info("KIS API Client initialized with host: ${properties.host}")
    }

    /**
     * API 접근 토큰 발급 받기
     * @return 토큰을 포함한 Mono
     */
    fun getAccessToken(): Mono<String> {
        // 토큰이 이미 있으면 재사용
        val existingToken = tokenRef.get()
        if (existingToken != null) {
            return Mono.just(existingToken)
        }

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
                tokenRef.set(newToken)
                logger.info("KIS API 접근 토큰 발급 완료")
                newToken
            }
    }
}
