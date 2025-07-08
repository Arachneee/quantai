package com.quantai.service.dto

/**
 * 채팅 요청 DTO
 */
data class ChatRequestDto(
    val message: String,
    val systemPrompt: String? = null,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
) 
