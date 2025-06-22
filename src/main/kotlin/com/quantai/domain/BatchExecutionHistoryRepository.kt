package com.quantai.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface BatchExecutionHistoryRepository : ReactiveMongoRepository<BatchExecutionHistory, String> {
    fun findByJobNameOrderByEndTimeDesc(jobName: String): Flux<BatchExecutionHistory>
    fun findByStartTimeBetween(start: LocalDateTime, end: LocalDateTime): Flux<BatchExecutionHistory>
}
