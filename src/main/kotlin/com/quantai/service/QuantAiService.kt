package com.quantai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.quantai.domain.DailyStockPrice
import com.quantai.domain.DailyStockPriceRepository
import com.quantai.domain.StockCodeRepository
import com.quantai.log.logger
import com.quantai.service.dto.ChatRequestDto
import com.quantai.service.dto.ChatResponseDto
import com.quantai.service.dto.StrongPerformingStockResponse
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class QuantAiService(
    private val chatClientBuilder: ChatClient.Builder,
    private val dailyStockPriceRepository: DailyStockPriceRepository,
    private val stockCodeRepository: StockCodeRepository,
) {
    private val logger = logger()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    val chatClient =
        chatClientBuilder
            .defaultSystem("Quantitative analyst focused on short-term stock opportunities.")
            .build()

    fun chat(request: ChatRequestDto): ChatResponseDto {
        val response =
            chatClient
                .prompt()
                .user(request.message)
                .call()
                .content() ?: "응답을 생성할 수 없습니다."

        return ChatResponseDto(response)
    }

    fun findStrongPerformingStocks(count: Int): List<StrongPerformingStockResponse> {
        return try {
            val actualStockPrices = getActualStockPrices()

            if (actualStockPrices.isEmpty()) {
                return emptyList()
            }

            val csvData = createCsvData(actualStockPrices)

            logger.info("CSV Data: $csvData")

            val response =
                chatClient
                    .prompt()
                    .user(
                        """
                        Analyze Korean stocks data. Select top $count for today's strong performance.
                        
                        **Available Data Fields:**
                        - Code: Stock code
                        - Name: Stock name
                        - ClosePrice: Closing price
                        - OpenPrice: Opening price
                        - HighPrice: Highest price
                        - LowPrice: Lowest price
                        - Volume: Trading volume
                        - FluctuationRate: Daily change percentage
                        - UpDownSign: Price direction (1=Up, 2=Down, 3=Unchanged)
                        - ComparedToYesterday: Price change amount

                        **Analysis Rules:**
                        1. Prioritize stocks with high trading volume
                        2. Focus on positive FluctuationRate (> 0)
                        3. Consider UpDownSign = 1 (upward trend)
                        4. Look for strong price momentum (high-low range)

                        Analyze the data below in CSV format:
                        `Code,Name,ClosePrice,OpenPrice,HighPrice,LowPrice,Volume,FluctuationRate,UpDownSign,ComparedToYesterday`

                        **Data:**
                        $csvData

                        Output MUST be valid JSON array: [{"stockCode":"005930","reason":"High volume with 3.2% gain"}]
                        """.trimIndent(),
                    ).call()
                    .content() ?: "[]"

            logger.info("Chat response: $response")

            return try {
                objectMapper.readValue<List<StrongPerformingStockResponse>>(response)
            } catch (e: Exception) {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getActualStockPrices(): List<DailyStockPrice> {
        val todayDate = LocalDate.now()
        val stockPrices = findStockPricesByDate(todayDate)

        return stockPrices.ifEmpty {
            findStockPricesByDate(todayDate.minusDays(1))
        }
    }

    private fun findStockPricesByDate(todayDate: LocalDate): List<DailyStockPrice> =
        dailyStockPriceRepository
            .findByDateOrderByStockCodeAsc(todayDate)
            .collectList()
            .block() ?: emptyList()

    private fun createCsvData(actualStockPrices: List<DailyStockPrice>): String {
        val stockCodes =
            stockCodeRepository
                .findAll()
                .collectList()
                .block() ?: emptyList()

        val stockCodeMap = stockCodes.associateBy { it.code }

        return actualStockPrices.joinToString("\n") { price ->
            val stockName = stockCodeMap[price.stockCode]?.name ?: "Unknown"
            "${price.stockCode},$stockName,${price.closePrice},${price.openPrice},${price.highPrice},${price.lowPrice},${price.volume},${price.fluctuationRate},${price.upDownSign},${price.comparedToYesterday}"
        }
    }
}
