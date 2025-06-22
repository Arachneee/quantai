package com.quantai.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StockCodeRepository : ReactiveMongoRepository<StockCode, String> {

    fun findByMarketCapNotNullOrderByMarketCapDesc(pageable: Pageable): Flux<StockCode>
}

fun StockCodeRepository.findMarketCapTop(pageable: Pageable): Flux<StockCode> {
    return findByMarketCapNotNullOrderByMarketCapDesc(pageable)
}
