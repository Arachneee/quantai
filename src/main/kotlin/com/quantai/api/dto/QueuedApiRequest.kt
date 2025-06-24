package com.quantai.api.dto

import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.MonoSink

data class QueuedApiRequest(
    val clientRequest: ClientRequest,
    val nextExchange: ExchangeFunction,
    val responseSink: MonoSink<ClientResponse>,
)
