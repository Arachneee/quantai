package com.quantai.api

import com.quantai.api.dto.TokenRequest
import com.quantai.api.dto.TokenResponse
import com.quantai.config.KisClientProperties
import com.quantai.log.logger
import jakarta.annotation.PostConstruct
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

abstract class KisClient(
    private val webClientBuilder: WebClient.Builder,
    private val properties: KisClientProperties,
) {
    protected val logger = logger()
    private val tokenRef = AtomicReference<String>(null)
    private val tokenExpired = AtomicReference<LocalDateTime>(null)
    protected val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    protected lateinit var webClient: WebClient

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
}
