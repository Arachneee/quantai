package com.quantai.service.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class WebSocketApprovalResponse(
    @JsonProperty("approval_key")
    val approvalKey: String,
)
