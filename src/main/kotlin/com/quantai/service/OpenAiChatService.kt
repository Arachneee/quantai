package com.quantai.service

import com.quantai.service.dto.ChatRequestDto
import com.quantai.service.dto.ChatResponseDto
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class OpenAiChatService(
    private val chatClientBuilder: ChatClient.Builder,
) {
    /**
     * OpenAI API를 통해 채팅 응답을 생성합니다.
     */
    fun chat(request: ChatRequestDto): ChatResponseDto {
        val chatClient =
            chatClientBuilder
                .defaultSystem(request.systemPrompt ?: "당신은 도움이 되는 AI 어시스턴트입니다.")
                .build()

        val response =
            chatClient
                .prompt()
                .user(request.message)
                .call()
                .content() ?: "응답을 생성할 수 없습니다."

        return ChatResponseDto(response)
    }
} 
