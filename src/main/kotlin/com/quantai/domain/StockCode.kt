package com.quantai.domain

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
@CompoundIndex(name = "marketCap_code_idx", def = "{'marketCap': 1, 'code': 1}")
data class StockCode(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val code: String,

    val marketType: MarketType? = null,

    val name: String? = null,

    val standardCode: String? = null,

    @Indexed
    val marketCap: Int? = null,

    val additionalAttributes: MutableMap<String, String> = mutableMapOf(),

    val lastUpdated: LocalDateTime = LocalDateTime.now()
)
