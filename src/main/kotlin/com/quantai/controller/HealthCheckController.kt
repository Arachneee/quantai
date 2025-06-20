package com.quantai.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HealthCheckController {
    @GetMapping("/health")
    fun hello(): Mono<String> {
        return Mono.just("true")
    }
}
