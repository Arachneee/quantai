package com.quantai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kis-client")
data class KisClientProperties(
    val host: String,
    val port: Int,
    val appKey: String,
    val appSecret: String,
    val customHeaders: Map<String, String> = emptyMap(),
)
