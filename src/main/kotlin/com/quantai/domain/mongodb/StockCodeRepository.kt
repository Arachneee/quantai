package com.quantai.domain.mongodb

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface StockCodeRepository : ReactiveMongoRepository<StockCode, String> {
}
