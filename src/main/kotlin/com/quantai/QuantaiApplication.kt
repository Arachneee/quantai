package com.quantai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuantaiApplication

fun main(args: Array<String>) {
    runApplication<QuantaiApplication>(*args)
}
