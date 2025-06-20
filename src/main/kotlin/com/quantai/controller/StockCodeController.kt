package com.quantai.controller

import com.quantai.domain.MarketType
import com.quantai.service.StockCodeExcelService
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/stocks")
class StockCodeController(
    private val stockCodeExcelService: StockCodeExcelService
) {
    /**
     * 엑셀 파일을 업로드하여 MongoDB에 종목 코드 정보 저장
     * @param filePart 업로드된 엑셀 파일
     * @param marketType 시장 유형 (KOSPI 또는 KOSDAQ)
     * @return 저장된 종목 코드 개수
     */
    @PutMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadStockCodes(
        @RequestPart("file") filePart: FilePart,
        @RequestParam("marketType") marketType: MarketType,
    ): Mono<Int> = stockCodeExcelService.processStockCodeExcel(filePart, marketType)
}
