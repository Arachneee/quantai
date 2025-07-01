package com.quantai.api

import com.quantai.config.properties.KisMockClientProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
@EnableConfigurationProperties(KisMockClientProperties::class)
class KisMockClient(
    private val webClientBuilder: WebClient.Builder,
    private val properties: KisMockClientProperties,
) : KisClient(webClientBuilder, properties)
