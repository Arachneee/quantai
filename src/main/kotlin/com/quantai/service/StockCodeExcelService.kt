package com.quantai.service

import com.quantai.domain.MarketType
import com.quantai.domain.StockCode
import com.quantai.domain.StockCodeRepository
import com.quantai.log.errorLog
import com.quantai.log.logger
import org.apache.poi.ss.usermodel.*
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.extra.math.sumAsInt

private const val i = 200

@Service
class StockCodeExcelService(
    private val stockCodeRepository: StockCodeRepository
) {
    private val logger = logger()

    fun processStockCodeExcel(filePart: FilePart, marketType: MarketType): Mono<Int> {
        return DataBufferUtils.join(filePart.content())
            .map { dataBuffer -> dataBuffer.asInputStream() }
            .flatMapMany {
                val workbook = WorkbookFactory.create(it)
                val sheet = workbook.getSheetAt(0)
                val headerIndexMap = createHeaderMap(sheet)

                Flux.create<StockCode> { emitter ->
                    workbook.use {
                        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                            try {
                                val row = sheet.getRow(rowIndex)
                                if (row != null) {
                                    emitter.next(createStockCode(row, headerIndexMap, marketType))
                                }
                            } catch (e: Exception) {
                                logger.errorLog(e) { "Could not emit StockCode in row : $rowIndex" }
                            }

                        }
                        emitter.complete()
                    }
                }
            }
            .filter { it.code.isNotBlank() }
            .buffer(BATCH_SIZE)
            .flatMap { stockCodeRepository.saveAll(it).count() }
            .sumAsInt()
    }

    private fun createHeaderMap(sheet: Sheet): Map<String, Int> {
        val headerRow = sheet.getRow(0) ?: return emptyMap()

        return (0 until headerRow.physicalNumberOfCells).associateBy { index ->
            headerRow.getCell(index)?.stringCellValue?.trim() ?: ""
        }
    }

    private fun createStockCode(
        row: Row,
        headerIndexMap: Map<String, Int>,
        marketType: MarketType
    ): StockCode = with(row) {
        StockCode(
            code = getCellValue(getCell(0)) ?: "",
            name = getCellValue(getCell(2)) ?: "",
            standardCode = getCellValue(getCell(1)) ?: "",
            marketCap = getNumericCellValue(getCell(getMarketCapIndex(headerIndexMap))),
            additionalAttributes = createAdditionalAttributes(headerIndexMap, this),
            marketType = marketType
        )
    }

    private fun getMarketCapIndex(headerIndexMap: Map<String, Int>) =
        headerIndexMap.getOrElse("시가총액") { headerIndexMap.getOrElse("전일기준 시가총액 (억)") { -1 } }

    private fun createAdditionalAttributes(
        headerIndexMap: Map<String, Int>,
        row: Row
    ): MutableMap<String, String> {
        return headerIndexMap
            .filter { (_, index) -> index > 2 }
            .mapValues { (_, index) -> getCellValue(row.getCell(index)) ?: "" }
            .filterValues { it.isNotEmpty() }
            .toMutableMap()
    }

    private fun getCellValue(cell: Cell?): String? {
        if (cell == null) return null

        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue.toString()
                } catch (e: IllegalStateException) {
                    try {
                        cell.stringCellValue.trim()
                    } catch (e: IllegalStateException) {
                        null
                    }
                }
            }
            CellType.BLANK -> null
            else -> null
        }
    }

    private fun getNumericCellValue(cell: Cell?): Int? {
        if (cell == null) return null

        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue.toInt()
            CellType.STRING -> cell.stringCellValue.trim().toIntOrNull()
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue.toInt()
                } catch (e: IllegalStateException) {
                    try {
                        cell.stringCellValue.trim().toIntOrNull()
                    } catch (e: IllegalStateException) {
                        null
                    }
                }
            }
            else -> null
        }
    }

    companion object {
        private const val BATCH_SIZE = 200
    }
}
