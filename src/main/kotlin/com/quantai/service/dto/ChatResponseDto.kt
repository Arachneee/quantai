package com.quantai.service.dto

/**
 * 채팅 응답 DTO
 */
data class ChatResponseDto(
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
)
