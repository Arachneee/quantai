package com.quantai.service.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class WebSocketApprovalRequest(
    @JsonProperty("grant_type")
    val grantType: String = "client_credentials",
    val appkey: String,
    val secretkey: String,
)
