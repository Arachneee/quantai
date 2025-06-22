package com.quantai.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

@Document(collection = "batch_execution_histories")
data class BatchExecutionHistory(
    @Id
    val id: String? = null,

    @Indexed
    val jobName: String,

    val uuid: String,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime,

    val dataStartDate: LocalDate,

    val dataEndDate: LocalDate,

    val stockCount: Int,

    val status: String,

    val errorMessage: String? = null
)
