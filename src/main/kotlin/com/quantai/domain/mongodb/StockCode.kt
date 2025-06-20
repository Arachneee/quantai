package com.quantai.domain.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * KOSPI/KOSDAQ 종목 정보를 저장하는 MongoDB 엔티티
 * 두 종류의 종목 데이터를 모두 수용할 수 있는 유연한 구조
 */
@Document(collection = "stock_codes")
@CompoundIndex(name = "market_code_idx", def = "{'marketType': 1, 'stockCode': 1}", unique = true)
data class StockCode(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val stockCode: String,  // 단축코드(종목코드) - KOSPI, KOSDAQ 공통

    @Indexed
    val marketType: String, // 시장 구분 (KOSPI, KOSDAQ)

    @Indexed
    val stockName: String,  // 한글 종목명 - KOSPI, KOSDAQ 공통

    val standardCode: String? = null,  // 표준코드 - KOSPI, KOSDAQ 공통

    val englishName: String? = null,   // 영문 종목명 - KOSPI, KOSDAQ 공통

    val sectorCode: String? = null,    // 업종 코드

    val sectorName: String? = null,    // 업종명

    val productGroup: String? = null,  // 상품 군

    val administrationType: String? = null, // 관리 구분

    val listingDate: String? = null,   // 상장일

    val securityGroup: String? = null, // 증권 구분

    val parValue: Double? = null,      // 액면가

    val listingCount: Long? = null,    // 상장주식수

    val financialSupervisor: String? = null, // 결산월

    val capital: Double? = null,       // 자본금

    val listedOrganization: String? = null, // 상장주관사

    // KOSDAQ 특화 필드
    val abbreviationCode: String? = null,  // 축약코드 - KOSDAQ

    val marketCapRank: Int? = null,    // 시가총액 순위

    val marketSegment: String? = null, // 시장 세그먼트 - KOSDAQ

    val investmentWarning: String? = null, // 투자주의환기 - KOSDAQ

    // 추가적인 필드들을 저장하기 위한 동적 맵
    val additionalAttributes: MutableMap<String, Any> = mutableMapOf(),

    val lastUpdated: LocalDateTime = LocalDateTime.now()  // 마지막 업데이트 시간
)
