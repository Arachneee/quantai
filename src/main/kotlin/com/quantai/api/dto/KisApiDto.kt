package com.quantai.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 토큰 발급 요청 DTO
 */
data class TokenRequest(
    @JsonProperty("grant_type")
    val grantType: String = "client_credentials",

    @JsonProperty("appkey")
    val appKey: String,

    @JsonProperty("appsecret")
    val appSecret: String,
)

/**
 * 토큰 발급 응답 DTO
 */
data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("access_token_token_expired")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val accessTokenExpired: LocalDateTime
)

/**
 * 주식 일별 시세 응답 데이터 구조
 */
data class StockPriceResponse(
    val output: List<DailyPrice> = emptyList(),

    @JsonProperty("rt_cd")
    val resultCode: String = "", // 성공 시 "0"

    @JsonProperty("msg_cd")
    val messageCode: String = "",

    @JsonProperty("msg1")
    val message: String = "",
)

/**
 * 일별 주가 정보
 */
data class DailyPrice(
    @JsonProperty("stck_bsop_date")
    val date: String, // 날짜

    @JsonProperty("stck_clpr")
    val closePrice: String, // 종가

    @JsonProperty("stck_oprc")
    val openPrice: String, // 시가

    @JsonProperty("stck_hgpr")
    val highPrice: String, // 고가

    @JsonProperty("stck_lwpr")
    val lowPrice: String, // 저가

    @JsonProperty("acml_vol")
    val volume: String, // 거래량

    @JsonProperty("acml_tr_pbmn")
    val tradingValue: String, // 거래대금

    @JsonProperty("fluc_rt")
    val fluctuationRate: String, // 등락률
)
