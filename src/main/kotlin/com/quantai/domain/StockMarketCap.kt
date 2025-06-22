package com.quantai.domain

class StockMarketCap(
    val id: String?,
    val stockCode: String,
    val marketCap: Int,
) {
    companion object {
        fun from(stockCode: StockCode): StockMarketCap {
            return StockMarketCap(stockCode.id, stockCode.code, stockCode.marketCap ?: 0)
        }
    }
}
