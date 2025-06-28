package com.quantai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "kis-client-mock")
data class KisMockClientProperties(
    override val host: String,
    override val port: Int,
    override val appKey: String,
    override val appSecret: String,
    override val maxRequestCountPerSec: Int,
    override val delayDuration: Duration,
) : KisClientProperties

@ConfigurationProperties(prefix = "kis-client-real")
data class KisRealClientProperties(
    override val host: String,
    override val port: Int,
    override val appKey: String,
    override val appSecret: String,
    override val maxRequestCountPerSec: Int,
    override val delayDuration: Duration,
) : KisClientProperties

interface KisClientProperties {
    val host: String
    val port: Int
    val appKey: String
    val appSecret: String
    val maxRequestCountPerSec: Int
    val delayDuration: Duration
}
