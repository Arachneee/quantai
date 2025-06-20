package com.quantai.service.dto

import com.quantai.domain.StockCode

data class StockMarketCapDto(val code: String, val marketCap: Int) {
    companion object {
        fun from(stockCode: StockCode) = StockMarketCapDto(stockCode.code, stockCode.marketCap ?: 0)
    }
}
