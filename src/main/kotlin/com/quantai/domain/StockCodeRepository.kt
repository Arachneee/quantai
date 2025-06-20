package com.quantai.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StockCodeRepository : ReactiveMongoRepository<StockCode, String> {
    fun findByMarketType(marketType: String): Flux<StockCode>
}
