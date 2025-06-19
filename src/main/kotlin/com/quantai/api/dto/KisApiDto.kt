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
 * 주식 일별 시세 응답 데이터 구조 (일자별 차트 형식)
 */
data class StockPriceResponse(
    // 헤더 정보
    @JsonProperty("output1")
    val header: StockPriceHeader? = null,

    // 일자별 시세 데이터 목록
    @JsonProperty("output2")
    val priceList: List<DailyChartPrice> = emptyList(),

    // API 응답 코드
    @JsonProperty("rt_cd")
    val resultCode: String = "", // 성공 시 "0"

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
    val comparedToYesterday: String = "", // 전일 대비

    @JsonProperty("prdy_vrss_sign")
    val upDownSign: String = "", // 전일 대비 부호(1:상한, 2:상승, 3:보합, 4:하한, 5:하락)

    @JsonProperty("stck_prpr")
    val currentPrice: String = "", // 현재가

    @JsonProperty("hts_kor_isnm")
    val stockName: String = "", // 종목명

    @JsonProperty("stck_shrn_iscd")
    val stockShortCode: String = "", // 단축 코드

    @JsonProperty("prdy_ctrt")
    val fluctuationRate: String = "", // 전일 대비 등락률
)

/**
 * 일자별 주가 차트 정보
 */
data class DailyChartPrice(
    @JsonProperty("stck_bsop_date")
    val date: String = "", // 날짜

    @JsonProperty("stck_clpr")
    val closePrice: String = "", // 종가

    @JsonProperty("stck_oprc")
    val openPrice: String = "", // 시가

    @JsonProperty("stck_hgpr")
    val highPrice: String = "", // 고가

    @JsonProperty("stck_lwpr")
    val lowPrice: String = "", // 저가

    @JsonProperty("acml_vol")
    val volume: String = "", // 거래량

    @JsonProperty("acml_tr_pbmn")
    val tradingValue: String = "", // 거래대금

    @JsonProperty("fluc_rt")
    val fluctuationRate: String = "", // 등락률

    @JsonProperty("prdy_vrss_sign")
    val upDownSign: String = "", // 전일 대비 부호

    @JsonProperty("prdy_vrss")
    val comparedToYesterday: String = "", // 전일 대비
)
