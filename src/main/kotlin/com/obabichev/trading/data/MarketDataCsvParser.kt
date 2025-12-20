package com.obabichev.trading.data

import com.obabichev.trading.model.MarketDataPoint
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Parses market data from CSV files in Yahoo Finance format.
 *
 * Expected format:
 * Date,Open,High,Low,Close,Adj Close,Volume
 * 2023-12-21,195.18,196.95,195.09,196.75,196.13,52242800
 */
object MarketDataCsvParser {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Parses a CSV file and returns a list of market data points.
     *
     * @param file CSV file to parse
     * @param symbol Stock symbol (used if not in filename)
     * @return List of market data points, sorted by date (oldest first)
     */
    fun parse(file: File, symbol: String? = null): List<MarketDataPoint> {
        require(file.exists()) { "File not found: ${file.absolutePath}" }

        val symbolToUse = symbol ?: file.nameWithoutExtension

        val lines = file.readLines()
        require(lines.isNotEmpty()) { "CSV file is empty: ${file.name}" }

        // Skip header line
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            parseLine(line, symbolToUse)
        }.sortedBy { it.date }
    }

    /**
     * Parses a single CSV line.
     */
    private fun parseLine(line: String, symbol: String): MarketDataPoint? {
        if (line.isBlank()) return null

        return try {
            val parts = line.split(',')
            require(parts.size >= 7) { "Invalid CSV line: $line" }

            MarketDataPoint(
                date = LocalDate.parse(parts[0].trim(), dateFormatter),
                symbol = symbol,
                open = BigDecimal(parts[1].trim()),
                high = BigDecimal(parts[2].trim()),
                low = BigDecimal(parts[3].trim()),
                close = BigDecimal(parts[4].trim()),
                adjClose = BigDecimal(parts[5].trim()),
                volume = parts[6].trim().toLong()
            )
        } catch (e: Exception) {
            println("⚠️  Warning: Failed to parse line: $line (${e.message})")
            null
        }
    }

    /**
     * Loads market data for a symbol from the tdata directory.
     */
    fun loadSymbol(symbol: String, dataDir: File = File("tdata")): List<MarketDataPoint> {
        val file = File(dataDir, "$symbol.csv")
        require(file.exists()) { "Market data file not found: ${file.absolutePath}" }

        println("Loading market data for $symbol from ${file.name}...")
        val data = parse(file, symbol)
        println("  ✅ Loaded ${data.size} data points (${data.first().date} to ${data.last().date})")

        return data
    }

    /**
     * Filters market data to a specific date range.
     */
    fun filterDateRange(
        data: List<MarketDataPoint>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MarketDataPoint> {
        return data.filter { it.date >= startDate && it.date <= endDate }
    }
}
