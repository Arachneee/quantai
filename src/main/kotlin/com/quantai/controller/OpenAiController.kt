package com.quantai.controller

import com.quantai.service.OpenAiChatService
import com.quantai.service.dto.ChatRequestDto
import com.quantai.service.dto.ChatResponseDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@RestController
@RequestMapping("/api/openai")
class OpenAiController(
    private val openAiChatService: OpenAiChatService,
) {
    /**
     * OpenAI API를 통해 채팅 응답을 생성합니다.
     *
     * @param request 채팅 요청 DTO
     * @return 채팅 응답 DTO
     */
    @PostMapping("/chat")
    fun chat(
        @RequestBody request: ChatRequestDto,
    ): Mono<ChatResponseDto> =
        Mono
            .fromCallable { openAiChatService.chat(request) }
            .subscribeOn(Schedulers.boundedElastic())
}
