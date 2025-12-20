package com.obabichev.trading.data

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Downloads historical market data from Yahoo Finance.
 *
 * Yahoo Finance provides free historical data via CSV download.
 * Data includes: Date, Open, High, Low, Close, Adj Close, Volume
 */
object YahooFinanceDownloader {

    private const val YAHOO_FINANCE_URL = "https://query1.finance.yahoo.com/v7/finance/download"
    private const val YAHOO_CHART_URL = "https://query2.finance.yahoo.com/v8/finance/chart"

    private data class YahooAuth(val cookie: String, val crumb: String)

    /**
     * Gets authentication cookie and crumb token from Yahoo Finance.
     */
    private fun getYahooAuth(symbol: String): YahooAuth? {
        return try {
            val url = URL("$YAHOO_CHART_URL/$symbol")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val cookies = connection.headerFields["Set-Cookie"]?.joinToString("; ") ?: ""
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            // Extract crumb from JSON response
            val crumbPattern = """"crumb":"([^"]+)"""".toRegex()
            val crumbMatch = crumbPattern.find(response)
            val crumb = crumbMatch?.groupValues?.get(1)

            if (cookies.isNotEmpty() && crumb != null) {
                YahooAuth(cookies, crumb)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Downloads historical data for a symbol and saves to CSV file.
     *
     * @param symbol Stock symbol (e.g., "AAPL", "TSLA")
     * @param startDate Start date for historical data
     * @param endDate End date for historical data
     * @param outputDir Directory to save CSV file
     * @return File if successful, null if failed
     */
    fun downloadSymbol(
        symbol: String,
        startDate: LocalDate,
        endDate: LocalDate,
        outputDir: File
    ): File? {
        println("Downloading $symbol...")

        // Get authentication
        val auth = getYahooAuth(symbol)
        if (auth == null) {
            println("  ❌ Failed to get authentication for $symbol")
            return null
        }

        val startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        val endTimestamp = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

        val url = "$YAHOO_FINANCE_URL/$symbol" +
                "?period1=$startTimestamp" +
                "&period2=$endTimestamp" +
                "&interval=1d" +
                "&events=history" +
                "&crumb=${auth.crumb}"

        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.setRequestProperty("Cookie", auth.cookie)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val csvData = connection.inputStream.bufferedReader().use { it.readText() }

                // Validate CSV has data
                val lines = csvData.lines().count()
                if (lines < 2) {
                    println("  ⚠️  No data returned for $symbol")
                    return null
                }

                // Save to file
                outputDir.mkdirs()
                val outputFile = File(outputDir, "$symbol.csv")
                outputFile.writeText(csvData)

                println("  ✅ $symbol: ${lines - 1} days of data saved to ${outputFile.name}")
                outputFile
            } else {
                println("  ❌ Failed to download $symbol: HTTP $responseCode")
                null
            }
        } catch (e: Exception) {
            println("  ❌ Error downloading $symbol: ${e.message}")
            null
        }
    }

    /**
     * Downloads historical data for multiple symbols.
     *
     * @param symbols List of stock symbols
     * @param startDate Start date for historical data
     * @param endDate End date for historical data
     * @param outputDir Directory to save CSV files
     * @return Map of symbol to File for successful downloads
     */
    fun downloadMultipleSymbols(
        symbols: List<String>,
        startDate: LocalDate,
        endDate: LocalDate,
        outputDir: File
    ): Map<String, File> {
        println("\n=== Yahoo Finance Data Download ===")
        println("Symbols: ${symbols.joinToString(", ")}")
        println("Period: $startDate to $endDate")
        println("Output: ${outputDir.absolutePath}")
        println("=" .repeat(35))
        println()

        val results = mutableMapOf<String, File>()

        symbols.forEach { symbol ->
            val file = downloadSymbol(symbol, startDate, endDate, outputDir)
            if (file != null) {
                results[symbol] = file
            }
            Thread.sleep(500) // Be nice to Yahoo Finance servers
        }

        println()
        println("=== Download Summary ===")
        println("Total symbols: ${symbols.size}")
        println("Successful: ${results.size}")
        println("Failed: ${symbols.size - results.size}")
        println("=" .repeat(24))

        return results
    }
}

/**
 * Main function to run the downloader as a standalone script.
 *
 * Usage: ./gradlew run --args="download-data"
 */
fun main(args: Array<String>) {
    // Configuration
    val symbols = listOf(
        "AAPL",  // Apple
        "TSLA",  // Tesla
        "MSFT",  // Microsoft
        "GOOGL", // Google
        "AMZN",  // Amazon
        "NVDA",  // NVIDIA
        "META",  // Meta (Facebook)
        "SPY"    // S&P 500 ETF
    )

    // Download last 2 years of data
    val endDate = LocalDate.now()
    val startDate = endDate.minus(2, ChronoUnit.YEARS)

    // Output directory
    val outputDir = File("tdata")

    // Download data
    val results = YahooFinanceDownloader.downloadMultipleSymbols(
        symbols = symbols,
        startDate = startDate,
        endDate = endDate,
        outputDir = outputDir
    )

    // Exit with status
    if (results.size == symbols.size) {
        println("\n✅ All downloads completed successfully!")
        System.exit(0)
    } else {
        println("\n⚠️  Some downloads failed. Check errors above.")
        System.exit(1)
    }
}
