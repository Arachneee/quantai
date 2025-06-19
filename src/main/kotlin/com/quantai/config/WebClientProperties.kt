package com.quantai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "webclient")
data class WebClientProperties(
    val maxInMemorySize: Int = 16777216,
    val connection: ConnectionProperties = ConnectionProperties(),
    val read: TimeoutProperties = TimeoutProperties(),
    val write: TimeoutProperties = TimeoutProperties(),
) {
    data class ConnectionProperties(
        val timeout: Duration = Duration.ofSeconds(5),
        val maxConnections: Int = 500,
        val maxIdleTime: Duration = Duration.ofSeconds(20),
        val maxLifeTime: Duration = Duration.ofSeconds(60),
        val pendingAcquireTimeout: Duration = Duration.ofSeconds(60),
        val evictInBackground: Duration = Duration.ofSeconds(120),
    )

    data class TimeoutProperties(
        val timeout: Duration = Duration.ofSeconds(5),
    )
}
