package com.quantai.service

import com.quantai.config.properties.KisWebSocketProperties
import com.quantai.log.errorLog
import com.quantai.log.logger
import jakarta.annotation.PreDestroy
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.Disposable
import reactor.core.publisher.SignalType
import reactor.util.retry.Retry
import java.util.concurrent.atomic.AtomicBoolean

@EnableConfigurationProperties(KisWebSocketProperties::class)
@Service
class KisWebSocketConnectionManager(
    private val webSocketClient: WebSocketClient,
    private val kisWebSocketProperties: KisWebSocketProperties,
    private val kisWebSocketHandler: KisWebSocketHandler,
) {
    val logger = logger()
    private val isConnected = AtomicBoolean(false)
    private var connectionDisposable: Disposable? = null

    @EventListener(ApplicationReadyEvent::class)
    fun connectOnStartup() {
        if (!isConnected.get()) {
            logger.info("[WebSocketConnectionManager] Application ready. Attempting initial WebSocket connection...")
            startWebSocketConnection()
        }
    }

    fun startWebSocketConnection() {
        if (isConnected.get()) {
            logger.info("[WebSocketConnectionManager] WebSocket connection already active.")
            return
        }

        logger.info("[WebSocketConnectionManager] Connecting to WebSocket: ${kisWebSocketProperties.websocketUri}")
        connectionDisposable =
            webSocketClient
                .execute(kisWebSocketProperties.websocketUri, kisWebSocketHandler)
                .doOnSubscribe {
                    isConnected.set(true)
                    logger.info("[WebSocketConnectionManager] WebSocket connection initiated.")
                }.doOnError { error ->
                    logger.errorLog(error) { "[WebSocketConnectionManager] WebSocket connection error" }
                    isConnected.set(false)
                }.doFinally { signalType ->
                    logger.info("[WebSocketConnectionManager] WebSocket connection finished with signal: $signalType.")
                    if (signalType != SignalType.ON_COMPLETE && signalType != SignalType.CANCEL) {
                        isConnected.set(false)
                        logger.info("[WebSocketConnectionManager] Attempting to reconnect...")
                    }
                }.retryWhen(
                    Retry
                        .backoff(Long.MAX_VALUE, kisWebSocketProperties.retryMinBackOff)
                        .maxBackoff(kisWebSocketProperties.retryMaxBackOff)
                        .doBeforeRetry { retrySignal ->
                            logger.info(
                                "[WebSocketConnectionManager] Retrying WebSocket connection. Attempt: ${retrySignal.totalRetries() + 1}",
                            )
                        },
                ).subscribe(
                    {},
                    { error -> logger.errorLog(error) { "[WebSocketConnectionManager] Final WebSocket connection failure" } },
                    { logger.info("[WebSocketConnectionManager] WebSocket connection successfully completed.") },
                )
    }

    @PreDestroy
    fun disconnectOnShutdown() {
        connectionDisposable?.dispose()
        isConnected.set(false)
        logger.info("[WebSocketConnectionManager] WebSocket connection disconnected during shutdown.")
    }
}
