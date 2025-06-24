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
    val accessTokenExpired: LocalDateTime,
)

/**
 * 주식 일별 시세 응답 데이터 구조 (일자별 차트 형식)
 */
data class StockPriceResponse(
    @JsonProperty("output1")
    val header: StockPriceHeader? = null,
    @JsonProperty("output2")
    val priceList: List<DailyChartPrice> = emptyList(),
    @JsonProperty("rt_cd")
    val resultCode: String = "",
    @JsonProperty("msg_cd")
    val messageCode: String = "",
    @JsonProperty("msg1")
    val message: String = "",
)

/**
 * 주식 시세 헤더 정보
 */
data class StockPriceHeader(
    @JsonProperty("prdy_vrss")
    val comparedToYesterday: String = "",
    @JsonProperty("prdy_vrss_sign")
    val upDownSign: String = "",
    @JsonProperty("stck_prpr")
    val currentPrice: String = "",
    @JsonProperty("hts_kor_isnm")
    val stockName: String = "",
    @JsonProperty("stck_shrn_iscd")
    val stockShortCode: String = "",
    @JsonProperty("prdy_ctrt")
    val fluctuationRate: String = "",
)

/**
 * 일자별 주가 차트 정보
 */
data class DailyChartPrice(
    @JsonProperty("stck_bsop_date")
    val date: String = "",
    @JsonProperty("stck_clpr")
    val closePrice: String = "",
    @JsonProperty("stck_oprc")
    val openPrice: String = "",
    @JsonProperty("stck_hgpr")
    val highPrice: String = "",
    @JsonProperty("stck_lwpr")
    val lowPrice: String = "",
    @JsonProperty("acml_vol")
    val volume: String = "",
    @JsonProperty("acml_tr_pbmn")
    val tradingValue: String = "",
    @JsonProperty("fluc_rt")
    val fluctuationRate: String = "",
    @JsonProperty("prdy_vrss_sign")
    val upDownSign: String = "",
    @JsonProperty("prdy_vrss")
    val comparedToYesterday: String = "",
)

/**
 * 주식 분봉 시세 응답 데이터 구조
 */
data class MinuteChartResponse(
    @JsonProperty("output1")
    val header: MinuteChartHeader? = null,
    @JsonProperty("output2")
    val priceList: List<MinuteChartPrice> = emptyList(),
    @JsonProperty("rt_cd")
    val resultCode: String = "",
    @JsonProperty("msg_cd")
    val messageCode: String = "",
    @JsonProperty("msg1")
    val message: String = "",
)

/**
 * 주식 분봉 시세 헤더 정보
 */
data class MinuteChartHeader(
    @JsonProperty("prdy_vrss")
    val comparedToYesterday: String = "",
    @JsonProperty("prdy_vrss_sign")
    val upDownSign: String = "",
    @JsonProperty("prdy_ctrt")
    val fluctuationRate: String = "",
    @JsonProperty("stck_prdy_clpr")
    val previousDayClosePrice: String = "",
    @JsonProperty("acml_vol")
    val accumulatedVolume: String = "",
    @JsonProperty("acml_tr_pbmn")
    val accumulatedTradeAmount: String = "",
    @JsonProperty("hts_kor_isnm")
    val stockName: String = "",
    @JsonProperty("stck_prpr")
    val currentPrice: String = "",
)

/**
 * 분봉 데이터 정보
 */
data class MinuteChartPrice(
    @JsonProperty("stck_bsop_date")
    val date: String = "",
    @JsonProperty("stck_cntg_hour")
    val time: String = "",
    @JsonProperty("stck_prpr")
    val currentPrice: String = "",
    @JsonProperty("stck_oprc")
    val openPrice: String = "",
    @JsonProperty("stck_hgpr")
    val highPrice: String = "",
    @JsonProperty("stck_lwpr")
    val lowPrice: String = "",
    @JsonProperty("cntg_vol")
    val volume: String = "",
    @JsonProperty("acml_tr_pbmn")
    val tradingValue: String = "",
)
