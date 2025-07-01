package com.quantai.api

import com.quantai.api.dto.QueuedApiRequest
import com.quantai.api.dto.TokenRequest
import com.quantai.api.dto.TokenResponse
import com.quantai.config.properties.KisClientProperties
import com.quantai.log.errorLog
import com.quantai.log.logger
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

abstract class KisClient(
    private val webClientBuilder: WebClient.Builder,
    private val properties: KisClientProperties,
) {
    protected val logger = logger()
    protected lateinit var webClient: WebClient
    protected val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    private val tokenRef = AtomicReference<String>(null)
    private val tokenExpired = AtomicReference<LocalDateTime>(null)

    private val requestSink: Sinks.Many<QueuedApiRequest> =
        Sinks.many().multicast().onBackpressureBuffer<QueuedApiRequest>()

    @PostConstruct
    fun initialize() {
        val baseUrl =
            if (properties.port > 0) {
                "${properties.host}:${properties.port}"
            } else {
                properties.host
            }

        webClient =
            webClientBuilder
                .baseUrl(baseUrl)
                .filter { request, next ->
                    Mono.create { sink ->
                        enqueueRequest(
                            QueuedApiRequest(request, next, sink),
                        )
                    }
                }.build()

        initRateLimiterQueue()

        getAccessToken().subscribe(
            { logger.info("Initial token acquired successfully") },
            { error -> logger.errorLog(error) { "Failed to acquire initial token" } },
        )

        logger.info("KIS API Client initialized with baseUrl: $baseUrl")
    }

    @PreDestroy
    fun shutdown() {
        requestSink.tryEmitComplete()
        logger.info("Request queue consumer shut down.")
    }

    private fun initRateLimiterQueue() {
        requestSink
            .asFlux()
            .delayElements(properties.delayDuration)
            .flatMap { request ->
                request.nextExchange
                    .exchange(request.clientRequest)
                    .doOnNext { response ->
                        request.responseSink.success(response)
                    }.doOnError { error ->
                        request.responseSink.error(error)
                    }.onErrorResume { error ->
                        logger.errorLog(error) { "Error processing request to ${request.clientRequest.url()}" }
                        Mono.empty()
                    }
            }.onErrorContinue { error, _ ->
                logger.errorLog(error) { "Error in queue consumer" }
            }.subscribe(
                { },
                { error ->
                    logger.errorLog(error) { "Critical error in queue consumer" }
                },
                { logger.info("Queue consumer completed") },
            )
    }

    private fun enqueueRequest(request: QueuedApiRequest) {
        val result = requestSink.tryEmitNext(request)
        if (result.isFailure) {
            val errorMessage = "Failed to enqueue request: $result for ${request.clientRequest.url()}"
            logger.error(errorMessage)
            request.responseSink.error(RuntimeException(errorMessage))
        }
    }

    fun getAccessToken(): Mono<String> {
        val existingToken = tokenRef.get()
        val existingTokenExpired = tokenExpired.get()
        if (existingToken != null &&
            existingTokenExpired != null &&
            LocalDateTime.now().isBefore(existingTokenExpired)
        ) {
            return Mono.just(existingToken)
        }

        return requestNewToken()
    }

    private fun requestNewToken(): Mono<String> {
        val request =
            TokenRequest(
                appKey = properties.appKey,
                appSecret = properties.appSecret,
            )

        return webClient
            .post()
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
            }.doOnError { error ->
                logger.errorLog(error) { "토큰 발급 중 오류 발생: ${error.message}" }
            }.retry(3) // 토큰 요청 실패 시 3번 재시도
    }
}
