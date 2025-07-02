package com.quantai.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 실시간 호가 데이터 모델 (H0UNASP0)
 * 한국투자증권 WebSocket API로부터 수신되는 실시간 호가 정보를 저장
 */
@Document(collection = "realtime_orderbooks")
@CompoundIndexes(
    CompoundIndex(name = "stock_timestamp_idx", def = "{'stockCode': 1, 'timestamp': 1}"),
    CompoundIndex(name = "timestamp_idx", def = "{'timestamp': 1}")
)
data class RealtimeOrderbook(
    @Id val id: String? = null,
    
    @Field("stock_code")
    val stockCode: String, // MKSC_SHRN_ISCD - 유가증권 단축 종목코드
    
    @Field("timestamp")
    val timestamp: LocalDateTime,
    
    @Field("business_time")
    val businessTime: String, // BSOP_HOUR - 영업 시간
    
    @Field("time_type_code")
    val timeTypeCode: String, // HOUR_CLS_CODE - 시간 구분 코드
    
    // 매도호가 1-10단계
    @Field("ask_price1")
    val askPrice1: BigDecimal, // ASKP1 - 매도호가1
    @Field("ask_price2")
    val askPrice2: BigDecimal, // ASKP2 - 매도호가2
    @Field("ask_price3")
    val askPrice3: BigDecimal, // ASKP3 - 매도호가3
    @Field("ask_price4")
    val askPrice4: BigDecimal, // ASKP4 - 매도호가4
    @Field("ask_price5")
    val askPrice5: BigDecimal, // ASKP5 - 매도호가5
    @Field("ask_price6")
    val askPrice6: BigDecimal, // ASKP6 - 매도호가6
    @Field("ask_price7")
    val askPrice7: BigDecimal, // ASKP7 - 매도호가7
    @Field("ask_price8")
    val askPrice8: BigDecimal, // ASKP8 - 매도호가8
    @Field("ask_price9")
    val askPrice9: BigDecimal, // ASKP9 - 매도호가9
    @Field("ask_price10")
    val askPrice10: BigDecimal, // ASKP10 - 매도호가10
    
    // 매수호가 1-10단계
    @Field("bid_price1")
    val bidPrice1: BigDecimal, // BIDP1 - 매수호가1
    @Field("bid_price2")
    val bidPrice2: BigDecimal, // BIDP2 - 매수호가2
    @Field("bid_price3")
    val bidPrice3: BigDecimal, // BIDP3 - 매수호가3
    @Field("bid_price4")
    val bidPrice4: BigDecimal, // BIDP4 - 매수호가4
    @Field("bid_price5")
    val bidPrice5: BigDecimal, // BIDP5 - 매수호가5
    @Field("bid_price6")
    val bidPrice6: BigDecimal, // BIDP6 - 매수호가6
    @Field("bid_price7")
    val bidPrice7: BigDecimal, // BIDP7 - 매수호가7
    @Field("bid_price8")
    val bidPrice8: BigDecimal, // BIDP8 - 매수호가8
    @Field("bid_price9")
    val bidPrice9: BigDecimal, // BIDP9 - 매수호가9
    @Field("bid_price10")
    val bidPrice10: BigDecimal, // BIDP10 - 매수호가10
    
    // 매도호가 잔량 1-10단계
    @Field("ask_quantity1")
    val askQuantity1: Long, // ASKP_RSQN1 - 매도호가 잔량1
    @Field("ask_quantity2")
    val askQuantity2: Long, // ASKP_RSQN2 - 매도호가 잔량2
    @Field("ask_quantity3")
    val askQuantity3: Long, // ASKP_RSQN3 - 매도호가 잔량3
    @Field("ask_quantity4")
    val askQuantity4: Long, // ASKP_RSQN4 - 매도호가 잔량4
    @Field("ask_quantity5")
    val askQuantity5: Long, // ASKP_RSQN5 - 매도호가 잔량5
    @Field("ask_quantity6")
    val askQuantity6: Long, // ASKP_RSQN6 - 매도호가 잔량6
    @Field("ask_quantity7")
    val askQuantity7: Long, // ASKP_RSQN7 - 매도호가 잔량7
    @Field("ask_quantity8")
    val askQuantity8: Long, // ASKP_RSQN8 - 매도호가 잔량8
    @Field("ask_quantity9")
    val askQuantity9: Long, // ASKP_RSQN9 - 매도호가 잔량9
    @Field("ask_quantity10")
    val askQuantity10: Long, // ASKP_RSQN10 - 매도호가 잔량10
    
    // 매수호가 잔량 1-10단계
    @Field("bid_quantity1")
    val bidQuantity1: Long, // BIDP_RSQN1 - 매수호가 잔량1
    @Field("bid_quantity2")
    val bidQuantity2: Long, // BIDP_RSQN2 - 매수호가 잔량2
    @Field("bid_quantity3")
    val bidQuantity3: Long, // BIDP_RSQN3 - 매수호가 잔량3
    @Field("bid_quantity4")
    val bidQuantity4: Long, // BIDP_RSQN4 - 매수호가 잔량4
    @Field("bid_quantity5")
    val bidQuantity5: Long, // BIDP_RSQN5 - 매수호가 잔량5
    @Field("bid_quantity6")
    val bidQuantity6: Long, // BIDP_RSQN6 - 매수호가 잔량6
    @Field("bid_quantity7")
    val bidQuantity7: Long, // BIDP_RSQN7 - 매수호가 잔량7
    @Field("bid_quantity8")
    val bidQuantity8: Long, // BIDP_RSQN8 - 매수호가 잔량8
    @Field("bid_quantity9")
    val bidQuantity9: Long, // BIDP_RSQN9 - 매수호가 잔량9
    @Field("bid_quantity10")
    val bidQuantity10: Long, // BIDP_RSQN10 - 매수호가 잔량10
    
    // 총 잔량 정보
    @Field("total_ask_quantity")
    val totalAskQuantity: Long, // TOTAL_ASKP_RSQN - 총 매도호가 잔량
    
    @Field("total_bid_quantity")
    val totalBidQuantity: Long, // TOTAL_BIDP_RSQN - 총 매수호가 잔량
    
    @Field("overtime_total_ask_quantity")
    val overtimeTotalAskQuantity: Long, // OVTM_TOTAL_ASKP_RSQN - 시간외 총 매도호가 잔량
    
    @Field("overtime_total_bid_quantity")
    val overtimeTotalBidQuantity: Long, // OVTM_TOTAL_BIDP_RSQN - 시간외 총 매수호가 잔량
    
    // 예상 체결 정보
    @Field("expected_execution_price")
    val expectedExecutionPrice: BigDecimal, // ANTC_CNPR - 예상 체결가
    
    @Field("expected_execution_quantity")
    val expectedExecutionQuantity: Long, // ANTC_CNQN - 예상 체결량
    
    @Field("expected_volume")
    val expectedVolume: Long, // ANTC_VOL - 예상 거래량
    
    @Field("expected_execution_diff")
    val expectedExecutionDiff: BigDecimal, // ANTC_CNTG_VRSS - 예상 체결 대비
    
    @Field("expected_execution_sign")
    val expectedExecutionSign: String, // ANTC_CNTG_VRSS_SIGN - 예상 체결 대비 부호
    
    @Field("expected_execution_rate")
    val expectedExecutionRate: BigDecimal, // ANTC_CNTG_PRDY_CTRT - 예상 체결 전일 대비율
    
    @Field("accumulated_volume")
    val accumulatedVolume: Long, // ACML_VOL - 누적 거래량
    
    // 잔량 증감 정보
    @Field("total_ask_quantity_change")
    val totalAskQuantityChange: Long, // TOTAL_ASKP_RSQN_ICDC - 총 매도호가 잔량 증감
    
    @Field("total_bid_quantity_change")
    val totalBidQuantityChange: Long, // TOTAL_BIDP_RSQN_ICDC - 총 매수호가 잔량 증감
    
    @Field("overtime_total_ask_change")
    val overtimeTotalAskChange: Long, // OVTM_TOTAL_ASKP_ICDC - 시간외 총 매도호가 증감
    
    @Field("overtime_total_bid_change")
    val overtimeTotalBidChange: Long, // OVTM_TOTAL_BIDP_ICDC - 시간외 총 매수호가 증감
    
    @Field("stock_deal_type_code")
    val stockDealTypeCode: String, // STCK_DEAL_CLS_CODE - 주식 매매 구분 코드
    
    // KRX 중간가 정보
    @Field("krx_mid_price")
    val krxMidPrice: BigDecimal, // KMID_PRC - KRX 중간가
    
    @Field("krx_mid_total_quantity")
    val krxMidTotalQuantity: Long, // KMID_TOTAL_RSQN - KRX 중간가잔량합계수량
    
    @Field("krx_mid_type_code")
    val krxMidTypeCode: String, // KMID_CLS_CODE - KRX 중간가 매수매도 구분
    
    // NXT 중간가 정보
    @Field("nxt_mid_price")
    val nxtMidPrice: BigDecimal, // NMID_PRC - NXT 중간가
    
    @Field("nxt_mid_total_quantity")
    val nxtMidTotalQuantity: Long, // NMID_TOTAL_RSQN - NXT 중간가잔량합계수량
    
    @Field("nxt_mid_type_code")
    val nxtMidTypeCode: String, // NMID_CLS_CODE - NXT 중간가 매수매도 구분
    
    @Field("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
