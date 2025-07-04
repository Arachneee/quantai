package com.quantai.config

import com.quantai.log.infoLog
import com.quantai.log.logger
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

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

@Configuration
@EnableConfigurationProperties(WebClientProperties::class)
class WebClientConfig(
    private val properties: WebClientProperties,
) {
    private val logger = logger()

    @Bean
    fun webClientBuilder(): WebClient.Builder =
        WebClient
            .builder()
            .clientConnector(createReactorClientConnector())
            .exchangeStrategies(createExchangeStrategies())
            .filter(logRequest())
            .filter(logResponse())

    private fun createReactorClientConnector(): ReactorClientHttpConnector = ReactorClientHttpConnector(createHttpClient())

    private fun createHttpClient(): HttpClient =
        HttpClient
            .create(createConnectionProvider())
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                properties.connection.timeout
                    .toMillis()
                    .toInt(),
            ).responseTimeout(properties.read.timeout)
            .doOnConnected { conn ->
                conn
                    .addHandlerLast(ReadTimeoutHandler(properties.read.timeout.toMillis(), TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(properties.write.timeout.toMillis(), TimeUnit.MILLISECONDS))
            }

    private fun createConnectionProvider(): ConnectionProvider =
        ConnectionProvider
            .builder("custom-provider")
            .also { providerBuilder ->
                with(properties.connection) {
                    providerBuilder
                        .maxConnections(maxConnections)
                        .maxIdleTime(maxIdleTime)
                        .maxLifeTime(maxLifeTime)
                        .pendingAcquireTimeout(pendingAcquireTimeout)
                        .evictInBackground(evictInBackground)
                }
            }.build()

    private fun createExchangeStrategies(): ExchangeStrategies =
        ExchangeStrategies
            .builder()
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(properties.maxInMemorySize) }
            .build()

    private fun logRequest(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { clientRequest: ClientRequest ->
            val headerStr = StringBuilder()
            clientRequest.headers().forEach { name, values ->
                headerStr.append("$name: ${values.joinToString(";")}").append(", ")
            }
            val headers = if (headerStr.isNotEmpty()) headerStr.substring(0, headerStr.length - 2) else ""

            logger.infoLog { "Request: ${clientRequest.method()} ${clientRequest.url()} Headers: [$headers]" }
            Mono.just(clientRequest)
        }

    private fun logResponse(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofResponseProcessor { clientResponse: ClientResponse ->
            logger.infoLog { "Response status: ${clientResponse.statusCode()}" }
            Mono.just(clientResponse)
        }
}
