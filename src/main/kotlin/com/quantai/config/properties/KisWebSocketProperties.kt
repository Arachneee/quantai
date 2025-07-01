package com.quantai.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration

@ConfigurationProperties(prefix = "kis-websocket")
data class KisWebSocketProperties(
    val websocketUri: URI,
    val retryMinBackOff: Duration,
    val retryMaxBackOff: Duration,
)
