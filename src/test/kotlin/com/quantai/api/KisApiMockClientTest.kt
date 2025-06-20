package com.quantai.api

import com.quantai.api.dto.TokenResponse
import com.quantai.config.KisClientMockProperties
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockKExtension::class)
class KisApiMockClientTest {

    @MockK
    lateinit var webClientBuilder: WebClient.Builder

    @MockK
    lateinit var properties: KisClientMockProperties

    @MockK
    lateinit var webClient: WebClient

    @MockK
    lateinit var requestBodyUriSpec: RequestBodyUriSpec

    @MockK
    lateinit var requestBodySpec: RequestBodySpec

    @MockK
    lateinit var responseSpec: ResponseSpec

    lateinit var kisApiMockClient: KisMockClient

    @BeforeEach
    fun setup() {
        // KisClientProperties 값 설정
        every { properties.host } returns "https://test-api.com"
        every { properties.appKey } returns "test-app-key"
        every { properties.appSecret } returns "test-app-secret"

        // WebClient 모킹
        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.build() } returns webClient
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.contentType(MediaType.APPLICATION_JSON) } returns requestBodySpec
        every { requestBodySpec.body(any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec

        // KisApiClient 생성 및 초기화
        kisApiMockClient = KisMockClient(webClientBuilder, properties)
        kisApiMockClient.initialize()
    }

    @Test
    fun `getAccessToken should return token from API response`() {
        // Given
        val expectedToken = "mock-access-token"
        val tokenResponse = TokenResponse(
            accessToken = expectedToken,
            tokenType = "Bearer",
            expiresIn = 86400
        )

        every { responseSpec.bodyToMono(TokenResponse::class.java) } returns Mono.just(tokenResponse)

        // When & Then
        StepVerifier.create(kisApiMockClient.getAccessToken())
            .expectNext(expectedToken)
            .verifyComplete()

        // Verify 호출 순서 및 인자
        verify {
            webClient.post()
            requestBodyUriSpec.uri("/oauth2/tokenP")
            requestBodySpec.contentType(MediaType.APPLICATION_JSON)
            responseSpec.bodyToMono(TokenResponse::class.java)
        }
    }

    @Test
    fun `getAccessToken should reuse existing token when available`() {
        // Given
        val expectedToken = "mock-access-token"
        val tokenResponse = TokenResponse(
            accessToken = expectedToken,
            tokenType = "Bearer",
            expiresIn = 86400
        )

        every { responseSpec.bodyToMono(TokenResponse::class.java) } returns Mono.just(tokenResponse)

        // 첫 번째 호출로 토큰 캐시
        StepVerifier.create(kisApiMockClient.getAccessToken())
            .expectNext(expectedToken)
            .verifyComplete()

        // 호출 카운트 초기화
        clearMocks(webClient)

        // When & Then: 이미 토큰이 있을 때 다시 호출
        StepVerifier.create(kisApiMockClient.getAccessToken())
            .expectNext(expectedToken)
            .verifyComplete()

        // Then: 토큰이 재사용되어 API 호출이 발생하지 않아야 함
        verify(exactly = 0) { webClient.post() }
    }
}

/**
 * 모의 객체 초기화를 위한 헬퍼 함수
 */
private fun clearMocks(vararg mocks: Any) {
    mocks.forEach {
        io.mockk.clearMocks(it)
    }
}
