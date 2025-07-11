package com.quantai.controller

import com.quantai.service.QuantAiService
import com.quantai.service.dto.ChatRequestDto
import com.quantai.service.dto.ChatResponseDto
import com.quantai.service.dto.StrongPerformingStockResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@RestController
@RequestMapping("/api/quantai")
class QuantAiController(
    private val quantAiService: QuantAiService,
) {
    @PostMapping("/chat")
    fun chat(
        @RequestBody request: ChatRequestDto,
    ): Mono<ChatResponseDto> =
        Mono
            .fromCallable { quantAiService.chat(request) }
            .subscribeOn(Schedulers.boundedElastic())

    @GetMapping("/strong-performing-stocks")
    fun findStrongPerformingStocks(count: Int = 20): Mono<List<StrongPerformingStockResponse>> =
        Mono
            .fromCallable { quantAiService.findStrongPerformingStocks(count) }
            .subscribeOn(Schedulers.boundedElastic())
}
