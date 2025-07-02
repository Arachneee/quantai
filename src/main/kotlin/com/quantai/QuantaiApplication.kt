package com.quantai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class QuantaiApplication

fun main(args: Array<String>) {
    runApplication<QuantaiApplication>(*args)
}
