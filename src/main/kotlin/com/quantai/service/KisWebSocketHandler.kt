package com.quantai.service

import com.quantai.log.errorLog
import com.quantai.log.logger
import com.quantai.service.dto.WebSocketApprovalRequest
import com.quantai.service.dto.WebSocketApprovalResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class KisWebSocketHandler(
    private val webClientBuilder: WebClient.Builder,
    private val stockService: StockService,
    private val realtimeDataParsingService: RealtimeDataParsingService,
    private val realtimeDataStorageService: RealtimeDataStorageService,
    @Value("\${kis-websocket.authDomain}") private val authDomain: String,
    @Value("\${kis-client-real.appKey}") private val appKey: String,
    @Value("\${kis-client-real.appSecret}") private val appSecret: String,
) : WebSocketHandler {
    private val logger = logger()

    private val webClient =
        webClientBuilder
            .baseUrl(authDomain)
            .build()

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val subscribeMessagesFlux =
            stockService
                .getMarketCapTop(0, 20)
                .map { it.code }
                .collectList()
                .zipWith(getWebSocketApprovalKey())
                .flatMapIterable { tuple ->
                    val stockCodes = tuple.t1
                    val approvalKey = tuple.t2
                    stockCodes.flatMap { stockCode ->
                        TR_IDS.map { trId -> createPayLoad(approvalKey, trId, stockCode) }
                    }
                }.map { session.textMessage(it) }

        val sendMono =
            session
                .send(subscribeMessagesFlux)
                .doOnSuccess { logger.info("웹소켓 메시지 전송 성공") }
                .doOnError { error ->
                    logger.errorLog(error) { "웹소켓 메시지 전송 중 오류 발생" }
                }

        val receiveFlow =
            session
                .receive()
                .doOnNext { message ->
                    val messageText = message.payloadAsText
                    logger.info("웹소켓 메시지 수신: $messageText")

                    // 메시지 파싱 및 저장
                    val parsedData = realtimeDataParsingService.parseWebSocketMessage(messageText)
                    if (parsedData != null) {
                        realtimeDataStorageService.saveRealtimeDataAsync(parsedData)
                            .doOnSuccess { 
                                logger.debug("실시간 데이터 저장 완료: ${parsedData::class.simpleName}")
                            }
                            .doOnError { error ->
                                logger.errorLog(error) { "실시간 데이터 저장 중 오류 발생" }
                            }
                            .subscribe()
                    }
                }.then()

        return sendMono.then(receiveFlow)
    }

    fun getWebSocketApprovalKey(): Mono<String> {
        val request =
            WebSocketApprovalRequest(
                appkey = appKey,
                secretkey = appSecret,
            )

        return webClient
            .post()
            .uri("/oauth2/Approval")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(WebSocketApprovalResponse::class.java)
            .map { it.approvalKey }
            .doOnError { error ->
                logger.errorLog(error) { "웹소켓 접속키 발급 중 오류 발생" }
            }
    }

    private fun createPayLoad(
        approvalKey: String,
        trId: String,
        stockCode: String,
    ): String =
        """
        {
          "header": {
            "approval_key": "$approvalKey",
            "custtype": "P",
            "tr_type": "1",
            "content-type": "utf-8"
          },
          "body": {
            "input": {
              "TR_ID": "$trId",
              "TR_KEY": "$stockCode"
            }
          }
        }
        """.trimIndent()

    companion object {
        private val TR_IDS = listOf("H0UNCNT0", "H0UNASP0") // 실시간 체결가, 실시간 호가
    }
}
