package com.obabichev.trading.data

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.random.Random

/**
 * Generates realistic sample market data for testing and development.
 *
 * Data follows Yahoo Finance CSV format:
 * Date,Open,High,Low,Close,Adj Close,Volume
 */
object SampleDataGenerator {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Generates sample market data for a symbol.
     *
     * @param symbol Stock symbol
     * @param startDate Start date
     * @param endDate End date
     * @param startingPrice Initial price for the simulation
     * @param volatility Price volatility (0.0 to 1.0, default 0.02 = 2% daily)
     * @return CSV content as string
     */
    fun generateSampleData(
        symbol: String,
        startDate: LocalDate,
        endDate: LocalDate,
        startingPrice: Double = 100.0,
        volatility: Double = 0.02
    ): String {
        val csv = StringBuilder()
        csv.appendLine("Date,Open,High,Low,Close,Adj Close,Volume")

        var currentDate = startDate
        var previousClose = startingPrice

        while (!currentDate.isAfter(endDate)) {
            // Skip weekends
            if (currentDate.dayOfWeek.value < 6) {
                val dayData = generateDayData(previousClose, volatility)
                csv.appendLine(
                    "${currentDate.format(dateFormatter)}," +
                            "${String.format("%.2f", dayData.open)}," +
                            "${String.format("%.2f", dayData.high)}," +
                            "${String.format("%.2f", dayData.low)}," +
                            "${String.format("%.2f", dayData.close)}," +
                            "${String.format("%.2f", dayData.adjClose)}," +
                            "${dayData.volume}"
                )
                previousClose = dayData.close
            }
            currentDate = currentDate.plusDays(1)
        }

        return csv.toString()
    }

    private data class DayData(
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val adjClose: Double,
        val volume: Long
    )

    private fun nextGaussian(): Double {
        // Box-Muller transform to generate Gaussian random numbers
        val u1 = Random.nextDouble()
        val u2 = Random.nextDouble()
        return kotlin.math.sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
    }

    private fun generateDayData(previousClose: Double, volatility: Double): DayData {
        // Random walk with drift
        val dailyReturn = nextGaussian() * volatility
        val open = previousClose * (1 + nextGaussian() * volatility * 0.3)

        // Generate intraday price action
        val close = open * (1 + dailyReturn)
        val high = maxOf(open, close) * (1 + abs(nextGaussian()) * volatility * 0.5)
        val low = minOf(open, close) * (1 - abs(nextGaussian()) * volatility * 0.5)

        // Adjusted close (same as close for sample data)
        val adjClose = close

        // Random volume (10M to 100M shares)
        val volume = (10_000_000 + Random.nextLong(90_000_000))

        return DayData(open, high, low, close, adjClose, volume)
    }

    /**
     * Generates and saves sample data for a symbol.
     */
    fun generateAndSave(
        symbol: String,
        startDate: LocalDate,
        endDate: LocalDate,
        outputDir: File,
        startingPrice: Double,
        volatility: Double = 0.02
    ): File {
        println("Generating sample data for $symbol...")

        val csvContent = generateSampleData(symbol, startDate, endDate, startingPrice, volatility)
        val lines = csvContent.lines().size - 1 // Subtract header

        outputDir.mkdirs()
        val outputFile = File(outputDir, "$symbol.csv")
        outputFile.writeText(csvContent)

        println("  ✅ $symbol: $lines days of sample data saved to ${outputFile.name}")
        return outputFile
    }

    /**
     * Generates sample data for multiple symbols with realistic starting prices.
     */
    fun generateMultipleSamples(
        symbols: List<String>,
        startDate: LocalDate,
        endDate: LocalDate,
        outputDir: File
    ): Map<String, File> {
        println("\n=== Sample Market Data Generator ===")
        println("Symbols: ${symbols.joinToString(", ")}")
        println("Period: $startDate to $endDate")
        println("Output: ${outputDir.absolutePath}")
        println("=" .repeat(36))
        println()

        // Realistic starting prices for common symbols
        val startingPrices = mapOf(
            "AAPL" to 180.0,
            "TSLA" to 240.0,
            "MSFT" to 370.0,
            "GOOGL" to 140.0,
            "AMZN" to 150.0,
            "NVDA" to 480.0,
            "META" to 350.0,
            "SPY" to 450.0
        )

        val results = mutableMapOf<String, File>()

        symbols.forEach { symbol ->
            val startingPrice = startingPrices[symbol] ?: 100.0
            val file = generateAndSave(symbol, startDate, endDate, outputDir, startingPrice)
            results[symbol] = file
        }

        println()
        println("=== Generation Summary ===")
        println("Total symbols: ${symbols.size}")
        println("Files created: ${results.size}")
        println("=" .repeat(26))
        println()
        println("📝 Note: This is sample data for testing.")
        println("   For real data, download from Yahoo Finance manually.")
        println("   See tdata/README.md for instructions.")

        return results
    }
}

/**
 * Main function to generate sample data.
 */
fun main() {
    val symbols = listOf(
        "AAPL",  // Apple
        "TSLA",  // Tesla
        "MSFT",  // Microsoft
        "GOOGL", // Google
        "AMZN",  // Amazon
        "NVDA",  // NVIDIA
        "META",  // Meta
        "SPY"    // S&P 500 ETF
    )

    val endDate = LocalDate.now()
    val startDate = endDate.minusYears(2)
    val outputDir = File("tdata")

    SampleDataGenerator.generateMultipleSamples(
        symbols = symbols,
        startDate = startDate,
        endDate = endDate,
        outputDir = outputDir
    )
}
