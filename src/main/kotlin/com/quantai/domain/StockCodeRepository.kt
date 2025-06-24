package com.quantai.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StockCodeRepository : ReactiveMongoRepository<StockCode, String> {
    @Query(
        value = "{ 'marketCap': { \$ne: null } }",
        sort = "{ 'marketCap': -1 }",
        fields = "{ 'id': 1, 'code': 1, 'marketCap': 1}",
    )
    fun findStockCodeRepositoryByMarketCapNotNullOrderByMarketCapDesc(pageable: Pageable): Flux<StockCode>
}

fun StockCodeRepository.findMarketCapTop(pageable: Pageable): Flux<StockMarketCap> {
    return findStockCodeRepositoryByMarketCapNotNullOrderByMarketCapDesc(pageable)
        .map {
            StockMarketCap.from(it)
        }
}
