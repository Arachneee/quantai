package com.quantai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantai.domain.RealtimeExecution
import com.quantai.domain.RealtimeOrderbook
import com.quantai.log.errorLog
import com.quantai.log.logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RealtimeDataParsingService(
    private val objectMapper: ObjectMapper,
) {
    private val logger = logger()

    companion object {
        private const val TR_ID_EXECUTION = "H0UNCNT0"
        private const val TR_ID_ORDERBOOK = "H0UNASP0"
        private val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    /**
     * WebSocket 메시지를 파싱하여 적절한 도메인 객체로 변환
     *
     * 메시지 형식: encryption_flag|TR_ID|data_count|response_data
     * 예시: 0|H0STCNT0|004|005930^123929^73100^5^...
     */
    fun parseWebSocketMessage(message: String): Any? {
        return try {
            // 첫 번째 응답이 JSON 형태인 경우 (등록 응답)
            if (message.startsWith("{")) {
                logger.debug("등록 응답 메시지 수신: $message")
                return null // 등록 응답은 처리하지 않음
            }

            // 파이프로 구분된 실시간 데이터 파싱
            val parts = message.split("|")
            if (parts.size < 4) {
                logger.warn("잘못된 메시지 형식: $message")
                return null
            }

            val encryptionFlag = parts[0]
            val trId = parts[1]
            val dataCount = parts[2].toIntOrNull() ?: 0
            val responseData = parts[3]

            logger.debug("파싱된 메시지 - 암호화: $encryptionFlag, TR_ID: $trId, 데이터 건수: $dataCount")

            // 암호화된 데이터는 현재 지원하지 않음
            if (encryptionFlag == "1") {
                logger.warn("암호화된 데이터는 현재 지원하지 않습니다: $message")
                return null
            }

            // 데이터 건수에 따라 여러 건의 데이터를 처리
            val dataRecords =
                if (dataCount > 1) {
                    // 여러 건의 데이터가 있는 경우 ^로 구분하여 각 레코드를 분리
                    splitMultipleRecords(responseData, dataCount)
                } else {
                    listOf(responseData)
                }

            // 첫 번째 레코드만 처리 (여러 건이 있어도 첫 번째만)
            val firstRecord = dataRecords.firstOrNull() ?: return null
            val fields = firstRecord.split("^")

            if (fields.isEmpty()) {
                logger.warn("응답 데이터가 비어있습니다: $message")
                return null
            }

            // 종목코드는 첫 번째 필드
            val stockCode = fields[0]

            when (trId) {
                TR_ID_EXECUTION -> parseExecutionData(fields, stockCode)
                TR_ID_ORDERBOOK -> parseOrderbookData(fields, stockCode)
                else -> {
                    logger.warn("알 수 없는 TR_ID: $trId")
                    null
                }
            }
        } catch (e: Exception) {
            logger.errorLog(e) { "WebSocket 메시지 파싱 중 오류 발생: $message" }
            null
        }
    }

    /**
     * 여러 건의 데이터 레코드를 분리
     */
    private fun splitMultipleRecords(
        responseData: String,
        dataCount: Int,
    ): List<String> {
        // 각 레코드는 고정된 필드 수를 가지므로 필드 수를 기준으로 분리
        val allFields = responseData.split("^")
        val fieldsPerRecord =
            when {
                responseData.contains("H0UNCNT0") -> 43 // 체결가 데이터 필드 수
                responseData.contains("H0UNASP0") -> 68 // 호가 데이터 필드 수
                else -> allFields.size / dataCount // 추정값
            }

        val records = mutableListOf<String>()
        for (i in 0 until dataCount) {
            val startIndex = i * fieldsPerRecord
            val endIndex = minOf(startIndex + fieldsPerRecord, allFields.size)
            if (startIndex < allFields.size) {
                val recordFields = allFields.subList(startIndex, endIndex)
                records.add(recordFields.joinToString("^"))
            }
        }

        return records
    }

    /**
     * 실시간 체결가 데이터 파싱 (H0UNCNT0)
     * 필드 순서는 API 문서의 Response Body 순서를 따름
     */
    private fun parseExecutionData(
        fields: List<String>,
        stockCode: String,
    ): RealtimeExecution? {
        return try {
            if (fields.size < 43) {
                logger.warn("체결가 데이터 필드 수가 부족합니다. 예상: 43, 실제: ${fields.size}")
                return null
            }

            RealtimeExecution(
                stockCode = stockCode, // fields[0] - MKSC_SHRN_ISCD
                timestamp = LocalDateTime.now(),
                executionTime = getField(fields, 1), // STCK_CNTG_HOUR
                currentPrice = parseBigDecimal(getField(fields, 2)), // STCK_PRPR
                previousDaySign = getField(fields, 3), // PRDY_VRSS_SIGN
                previousDayDiff = parseBigDecimal(getField(fields, 4)), // PRDY_VRSS
                previousDayRate = parseBigDecimal(getField(fields, 5)), // PRDY_CTRT
                weightedAvgPrice = parseBigDecimal(getField(fields, 6)), // WGHN_AVRG_STCK_PRC
                openPrice = parseBigDecimal(getField(fields, 7)), // STCK_OPRC
                highPrice = parseBigDecimal(getField(fields, 8)), // STCK_HGPR
                lowPrice = parseBigDecimal(getField(fields, 9)), // STCK_LWPR
                askPrice1 = parseBigDecimal(getField(fields, 10)), // ASKP1
                bidPrice1 = parseBigDecimal(getField(fields, 11)), // BIDP1
                executionVolume = parseLong(getField(fields, 12)), // CNTG_VOL
                accumulatedVolume = parseLong(getField(fields, 13)), // ACML_VOL
                accumulatedTradingValue = parseLong(getField(fields, 14)), // ACML_TR_PBMN
                sellExecutionCount = parseInt(getField(fields, 15)), // SELN_CNTG_CSNU
                buyExecutionCount = parseInt(getField(fields, 16)), // SHNU_CNTG_CSNU
                netBuyExecutionCount = parseInt(getField(fields, 17)), // NTBY_CNTG_CSNU
                executionStrength = parseBigDecimal(getField(fields, 18)), // CTTR
                totalSellQuantity = parseLong(getField(fields, 19)), // SELN_CNTG_SMTN
                totalBuyQuantity = parseLong(getField(fields, 20)), // SHNU_CNTG_SMTN
                executionTypeCode = getField(fields, 21), // CNTG_CLS_CODE
                buyRate = parseBigDecimal(getField(fields, 22)), // SHNU_RATE
                volumeRateVsYesterday = parseBigDecimal(getField(fields, 23)), // PRDY_VOL_VRSS_ACML_VOL_RATE
                openTime = getField(fields, 24), // OPRC_HOUR
                openVsCurrentSign = getField(fields, 25), // OPRC_VRSS_PRPR_SIGN
                openVsCurrent = parseBigDecimal(getField(fields, 26)), // OPRC_VRSS_PRPR
                highTime = getField(fields, 27), // HGPR_HOUR
                highVsCurrentSign = getField(fields, 28), // HGPR_VRSS_PRPR_SIGN
                highVsCurrent = parseBigDecimal(getField(fields, 29)), // HGPR_VRSS_PRPR
                lowTime = getField(fields, 30), // LWPR_HOUR
                lowVsCurrentSign = getField(fields, 31), // LWPR_VRSS_PRPR_SIGN
                lowVsCurrent = parseBigDecimal(getField(fields, 32)), // LWPR_VRSS_PRPR
                businessDate = getField(fields, 33), // BSOP_DATE
                marketOperationCode = getField(fields, 34), // NEW_MKOP_CLS_CODE
                tradingHaltYn = getField(fields, 35), // TRHT_YN
                askQuantity1 = parseLong(getField(fields, 36)), // ASKP_RSQN1
                bidQuantity1 = parseLong(getField(fields, 37)), // BIDP_RSQN1
                totalAskQuantity = parseLong(getField(fields, 38)), // TOTAL_ASKP_RSQN
                totalBidQuantity = parseLong(getField(fields, 39)), // TOTAL_BIDP_RSQN
                volumeTurnoverRate = parseBigDecimal(getField(fields, 40)), // VOL_TNRT
                yesterdaySameTimeVolume = parseLong(getField(fields, 41)), // PRDY_SMNS_HOUR_ACML_VOL
                yesterdaySameTimeVolumeRate = parseBigDecimal(getField(fields, 42)), // PRDY_SMNS_HOUR_ACML_VOL_RATE
                timeTypeCode = getField(fields, 43), // HOUR_CLS_CODE
                marketCloseCode = getField(fields, 44), // MRKT_TRTM_CLS_CODE
                viStandardPrice = parseBigDecimal(getField(fields, 45)), // VI_STND_PRC
            )
        } catch (e: Exception) {
            logger.errorLog(e) { "실시간 체결가 데이터 파싱 중 오류 발생" }
            null
        }
    }

    /**
     * 실시간 호가 데이터 파싱 (H0UNASP0)
     * 필드 순서는 API 문서의 Response Body 순서를 따름
     */
    private fun parseOrderbookData(
        fields: List<String>,
        stockCode: String,
    ): RealtimeOrderbook? {
        return try {
            if (fields.size < 68) {
                logger.warn("호가 데이터 필드 수가 부족합니다. 예상: 68, 실제: ${fields.size}")
                return null
            }

            RealtimeOrderbook(
                stockCode = stockCode, // fields[0] - MKSC_SHRN_ISCD
                timestamp = LocalDateTime.now(),
                businessTime = getField(fields, 1), // BSOP_HOUR
                timeTypeCode = getField(fields, 2), // HOUR_CLS_CODE
                // 매도호가 1-10단계
                askPrice1 = parseBigDecimal(getField(fields, 3)), // ASKP1
                askPrice2 = parseBigDecimal(getField(fields, 4)), // ASKP2
                askPrice3 = parseBigDecimal(getField(fields, 5)), // ASKP3
                askPrice4 = parseBigDecimal(getField(fields, 6)), // ASKP4
                askPrice5 = parseBigDecimal(getField(fields, 7)), // ASKP5
                askPrice6 = parseBigDecimal(getField(fields, 8)), // ASKP6
                askPrice7 = parseBigDecimal(getField(fields, 9)), // ASKP7
                askPrice8 = parseBigDecimal(getField(fields, 10)), // ASKP8
                askPrice9 = parseBigDecimal(getField(fields, 11)), // ASKP9
                askPrice10 = parseBigDecimal(getField(fields, 12)), // ASKP10
                // 매수호가 1-10단계
                bidPrice1 = parseBigDecimal(getField(fields, 13)), // BIDP1
                bidPrice2 = parseBigDecimal(getField(fields, 14)), // BIDP2
                bidPrice3 = parseBigDecimal(getField(fields, 15)), // BIDP3
                bidPrice4 = parseBigDecimal(getField(fields, 16)), // BIDP4
                bidPrice5 = parseBigDecimal(getField(fields, 17)), // BIDP5
                bidPrice6 = parseBigDecimal(getField(fields, 18)), // BIDP6
                bidPrice7 = parseBigDecimal(getField(fields, 19)), // BIDP7
                bidPrice8 = parseBigDecimal(getField(fields, 20)), // BIDP8
                bidPrice9 = parseBigDecimal(getField(fields, 21)), // BIDP9
                bidPrice10 = parseBigDecimal(getField(fields, 22)), // BIDP10
                // 매도호가 잔량 1-10단계
                askQuantity1 = parseLong(getField(fields, 23)), // ASKP_RSQN1
                askQuantity2 = parseLong(getField(fields, 24)), // ASKP_RSQN2
                askQuantity3 = parseLong(getField(fields, 25)), // ASKP_RSQN3
                askQuantity4 = parseLong(getField(fields, 26)), // ASKP_RSQN4
                askQuantity5 = parseLong(getField(fields, 27)), // ASKP_RSQN5
                askQuantity6 = parseLong(getField(fields, 28)), // ASKP_RSQN6
                askQuantity7 = parseLong(getField(fields, 29)), // ASKP_RSQN7
                askQuantity8 = parseLong(getField(fields, 30)), // ASKP_RSQN8
                askQuantity9 = parseLong(getField(fields, 31)), // ASKP_RSQN9
                askQuantity10 = parseLong(getField(fields, 32)), // ASKP_RSQN10
                // 매수호가 잔량 1-10단계
                bidQuantity1 = parseLong(getField(fields, 33)), // BIDP_RSQN1
                bidQuantity2 = parseLong(getField(fields, 34)), // BIDP_RSQN2
                bidQuantity3 = parseLong(getField(fields, 35)), // BIDP_RSQN3
                bidQuantity4 = parseLong(getField(fields, 36)), // BIDP_RSQN4
                bidQuantity5 = parseLong(getField(fields, 37)), // BIDP_RSQN5
                bidQuantity6 = parseLong(getField(fields, 38)), // BIDP_RSQN6
                bidQuantity7 = parseLong(getField(fields, 39)), // BIDP_RSQN7
                bidQuantity8 = parseLong(getField(fields, 40)), // BIDP_RSQN8
                bidQuantity9 = parseLong(getField(fields, 41)), // BIDP_RSQN9
                bidQuantity10 = parseLong(getField(fields, 42)), // BIDP_RSQN10
                // 총 잔량 정보
                totalAskQuantity = parseLong(getField(fields, 43)), // TOTAL_ASKP_RSQN
                totalBidQuantity = parseLong(getField(fields, 44)), // TOTAL_BIDP_RSQN
                overtimeTotalAskQuantity = parseLong(getField(fields, 45)), // OVTM_TOTAL_ASKP_RSQN
                overtimeTotalBidQuantity = parseLong(getField(fields, 46)), // OVTM_TOTAL_BIDP_RSQN
                // 예상 체결 정보
                expectedExecutionPrice = parseBigDecimal(getField(fields, 47)), // ANTC_CNPR
                expectedExecutionQuantity = parseLong(getField(fields, 48)), // ANTC_CNQN
                expectedVolume = parseLong(getField(fields, 49)), // ANTC_VOL
                expectedExecutionDiff = parseBigDecimal(getField(fields, 50)), // ANTC_CNTG_VRSS
                expectedExecutionSign = getField(fields, 51), // ANTC_CNTG_VRSS_SIGN
                expectedExecutionRate = parseBigDecimal(getField(fields, 52)), // ANTC_CNTG_PRDY_CTRT
                accumulatedVolume = parseLong(getField(fields, 53)), // ACML_VOL
                // 잔량 증감 정보
                totalAskQuantityChange = parseLong(getField(fields, 54)), // TOTAL_ASKP_RSQN_ICDC
                totalBidQuantityChange = parseLong(getField(fields, 55)), // TOTAL_BIDP_RSQN_ICDC
                overtimeTotalAskChange = parseLong(getField(fields, 56)), // OVTM_TOTAL_ASKP_ICDC
                overtimeTotalBidChange = parseLong(getField(fields, 57)), // OVTM_TOTAL_BIDP_ICDC
                stockDealTypeCode = getField(fields, 58), // STCK_DEAL_CLS_CODE
                // KRX 중간가 정보
                krxMidPrice = parseBigDecimal(getField(fields, 59)), // KMID_PRC
                krxMidTotalQuantity = parseLong(getField(fields, 60)), // KMID_TOTAL_RSQN
                krxMidTypeCode = getField(fields, 61), // KMID_CLS_CODE
                // NXT 중간가 정보
                nxtMidPrice = parseBigDecimal(getField(fields, 62)), // NMID_PRC
                nxtMidTotalQuantity = parseLong(getField(fields, 63)), // NMID_TOTAL_RSQN
                nxtMidTypeCode = getField(fields, 64), // NMID_CLS_CODE
            )
        } catch (e: Exception) {
            logger.errorLog(e) { "실시간 호가 데이터 파싱 중 오류 발생" }
            null
        }
    }

    /**
     * 필드 배열에서 안전하게 값을 가져오는 헬퍼 메서드
     */
    private fun getField(
        fields: List<String>,
        index: Int,
    ): String = if (index < fields.size) fields[index] else ""

    /**
     * 문자열을 BigDecimal로 안전하게 변환
     */
    private fun parseBigDecimal(value: String): BigDecimal =
        try {
            if (value.isBlank()) {
                BigDecimal.ZERO
            } else {
                BigDecimal(value.replace(",", ""))
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

    /**
     * 문자열을 Long으로 안전하게 변환
     */
    private fun parseLong(value: String): Long =
        try {
            if (value.isBlank()) {
                0L
            } else {
                value.replace(",", "").toLong()
            }
        } catch (e: Exception) {
            0L
        }

    /**
     * 문자열을 Int로 안전하게 변환
     */
    private fun parseInt(value: String): Int =
        try {
            if (value.isBlank()) {
                0
            } else {
                value.replace(",", "").toInt()
            }
        } catch (e: Exception) {
            0
        }
}
