package com.quantai.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 실시간 체결가 데이터 모델 (H0UNCNT0)
 * 한국투자증권 WebSocket API로부터 수신되는 실시간 체결가 정보를 저장
 */
@Document(collection = "realtime_executions")
@CompoundIndexes(
    CompoundIndex(name = "stock_timestamp_idx", def = "{'stockCode': 1, 'timestamp': 1}"),
    CompoundIndex(name = "timestamp_idx", def = "{'timestamp': 1}")
)
data class RealtimeExecution(
    @Id val id: String? = null,

    @Field("stock_code")
    val stockCode: String, // MKSC_SHRN_ISCD - 유가증권 단축 종목코드

    @Field("timestamp")
    val timestamp: LocalDateTime,

    @Field("execution_time")
    val executionTime: String, // STCK_CNTG_HOUR - 주식 체결 시간

    @Field("current_price")
    val currentPrice: BigDecimal, // STCK_PRPR - 주식 현재가

    @Field("previous_day_sign")
    val previousDaySign: String, // PRDY_VRSS_SIGN - 전일 대비 부호

    @Field("previous_day_diff")
    val previousDayDiff: BigDecimal, // PRDY_VRSS - 전일 대비

    @Field("previous_day_rate")
    val previousDayRate: BigDecimal, // PRDY_CTRT - 전일 대비율

    @Field("weighted_avg_price")
    val weightedAvgPrice: BigDecimal, // WGHN_AVRG_STCK_PRC - 가중 평균 주식 가격

    @Field("open_price")
    val openPrice: BigDecimal, // STCK_OPRC - 주식 시가

    @Field("high_price")
    val highPrice: BigDecimal, // STCK_HGPR - 주식 최고가

    @Field("low_price")
    val lowPrice: BigDecimal, // STCK_LWPR - 주식 최저가

    @Field("ask_price1")
    val askPrice1: BigDecimal, // ASKP1 - 매도호가1

    @Field("bid_price1")
    val bidPrice1: BigDecimal, // BIDP1 - 매수호가1

    @Field("execution_volume")
    val executionVolume: Long, // CNTG_VOL - 체결 거래량

    @Field("accumulated_volume")
    val accumulatedVolume: Long, // ACML_VOL - 누적 거래량

    @Field("accumulated_trading_value")
    val accumulatedTradingValue: Long, // ACML_TR_PBMN - 누적 거래 대금

    @Field("sell_execution_count")
    val sellExecutionCount: Int, // SELN_CNTG_CSNU - 매도 체결 건수

    @Field("buy_execution_count")
    val buyExecutionCount: Int, // SHNU_CNTG_CSNU - 매수 체결 건수

    @Field("net_buy_execution_count")
    val netBuyExecutionCount: Int, // NTBY_CNTG_CSNU - 순매수 체결 건수

    @Field("execution_strength")
    val executionStrength: BigDecimal, // CTTR - 체결강도

    @Field("total_sell_quantity")
    val totalSellQuantity: Long, // SELN_CNTG_SMTN - 총 매도 수량

    @Field("total_buy_quantity")
    val totalBuyQuantity: Long, // SHNU_CNTG_SMTN - 총 매수 수량

    @Field("execution_type_code")
    val executionTypeCode: String, // CNTG_CLS_CODE - 체결구분

    @Field("buy_rate")
    val buyRate: BigDecimal, // SHNU_RATE - 매수비율

    @Field("volume_rate_vs_yesterday")
    val volumeRateVsYesterday: BigDecimal, // PRDY_VOL_VRSS_ACML_VOL_RATE - 전일 거래량 대비 등락율

    @Field("open_time")
    val openTime: String, // OPRC_HOUR - 시가 시간

    @Field("open_vs_current_sign")
    val openVsCurrentSign: String, // OPRC_VRSS_PRPR_SIGN - 시가대비구분

    @Field("open_vs_current")
    val openVsCurrent: BigDecimal, // OPRC_VRSS_PRPR - 시가대비

    @Field("high_time")
    val highTime: String, // HGPR_HOUR - 최고가 시간

    @Field("high_vs_current_sign")
    val highVsCurrentSign: String, // HGPR_VRSS_PRPR_SIGN - 고가대비구분

    @Field("high_vs_current")
    val highVsCurrent: BigDecimal, // HGPR_VRSS_PRPR - 고가대비

    @Field("low_time")
    val lowTime: String, // LWPR_HOUR - 최저가 시간

    @Field("low_vs_current_sign")
    val lowVsCurrentSign: String, // LWPR_VRSS_PRPR_SIGN - 저가대비구분

    @Field("low_vs_current")
    val lowVsCurrent: BigDecimal, // LWPR_VRSS_PRPR - 저가대비

    @Field("business_date")
    val businessDate: String, // BSOP_DATE - 영업 일자

    @Field("market_operation_code")
    val marketOperationCode: String, // NEW_MKOP_CLS_CODE - 신 장운영 구분 코드

    @Field("trading_halt_yn")
    val tradingHaltYn: String, // TRHT_YN - 거래정지 여부

    @Field("ask_quantity1")
    val askQuantity1: Long, // ASKP_RSQN1 - 매도호가 잔량1

    @Field("bid_quantity1")
    val bidQuantity1: Long, // BIDP_RSQN1 - 매수호가 잔량1

    @Field("total_ask_quantity")
    val totalAskQuantity: Long, // TOTAL_ASKP_RSQN - 총 매도호가 잔량

    @Field("total_bid_quantity")
    val totalBidQuantity: Long, // TOTAL_BIDP_RSQN - 총 매수호가 잔량

    @Field("volume_turnover_rate")
    val volumeTurnoverRate: BigDecimal, // VOL_TNRT - 거래량 회전율

    @Field("yesterday_same_time_volume")
    val yesterdaySameTimeVolume: Long, // PRDY_SMNS_HOUR_ACML_VOL - 전일 동시간 누적 거래량

    @Field("yesterday_same_time_volume_rate")
    val yesterdaySameTimeVolumeRate: BigDecimal, // PRDY_SMNS_HOUR_ACML_VOL_RATE - 전일 동시간 누적 거래량 비율

    @Field("time_type_code")
    val timeTypeCode: String, // HOUR_CLS_CODE - 시간 구분 코드

    @Field("market_close_code")
    val marketCloseCode: String, // MRKT_TRTM_CLS_CODE - 임의종료구분코드

    @Field("vi_standard_price")
    val viStandardPrice: BigDecimal, // VI_STND_PRC - 정적VI발동기준가

    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
