package com.quantai.service.dto

import com.quantai.domain.StockMarketCap

data class StockMarketCapDto(val code: String, val marketCap: Int) {
    companion object {
        fun from(stockMarketCap: StockMarketCap) =
            StockMarketCapDto(stockMarketCap.stockCode, stockMarketCap.marketCap ?: 0)
    }
}
