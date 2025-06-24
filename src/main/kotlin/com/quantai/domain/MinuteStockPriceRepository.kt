package com.quantai.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MinuteStockPriceRepository : ReactiveMongoRepository<MinuteStockPrice, String> {
}
