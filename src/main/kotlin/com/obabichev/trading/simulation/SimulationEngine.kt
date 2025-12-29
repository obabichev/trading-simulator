package com.obabichev.trading.simulation

import com.obabichev.trading.data.MarketDataCsvParser
import com.obabichev.trading.model.*
import com.obabichev.trading.strategy.TradingStrategy
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.sqrt

/**
 * Runs trading strategy simulations and calculates performance metrics.
 */
class SimulationEngine(
    private val strategy: TradingStrategy,
    private val initialBudget: BigDecimal
) {
    fun runSimulation(
        symbol: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): SimulationResult {
        val startTime = System.currentTimeMillis()

        println("\n" + "=".repeat(50))
        println("Starting Simulation")
        println("=".repeat(50))
        println("Strategy: ${strategy.name}")
        println("Symbol: $symbol")
        println("Initial Budget: $$initialBudget")

        // Load market data
        val allData = MarketDataCsvParser.loadSymbol(symbol)

        // Filter to date range if specified
        val data = if (startDate != null && endDate != null) {
            val filtered = MarketDataCsvParser.filterDateRange(allData, startDate, endDate)
            println("Date Range: $startDate to $endDate")
            filtered
        } else {
            println("Date Range: ${allData.first().date} to ${allData.last().date}")
            allData
        }

        require(data.isNotEmpty()) { "No market data available for simulation" }

        println("Data Points: ${data.size}")
        println("=".repeat(50))

        // Initialize
        strategy.initialize()
        var portfolio = Portfolio(cash = initialBudget)
        val trades = mutableListOf<Trade>()
        val portfolioHistory = mutableListOf<Pair<LocalDate, BigDecimal>>()

        // Run simulation day by day
        data.forEachIndexed { index, dataPoint ->
            val historicalData = data.subList(0, index + 1)

            // Get strategy decision
            val trade = strategy.onData(dataPoint, portfolio, historicalData)

            // Execute trade if any
            if (trade != null) {
                portfolio = when (trade.action) {
                    TradeAction.BUY -> portfolio.buy(trade.symbol, trade.shares, trade.price)
                    TradeAction.SELL -> portfolio.sell(trade.symbol, trade.shares, trade.price)
                }
                trades.add(trade)

                println("\n${trade.action} on ${trade.date}")
                println("  Shares: ${trade.shares.setScale(2, RoundingMode.HALF_UP)}")
                println("  Price: $${trade.price.setScale(2, RoundingMode.HALF_UP)}")
                println("  Total: $${trade.totalAmount.setScale(2, RoundingMode.HALF_UP)}")
            }

            // Track portfolio value
            val currentValue = portfolio.totalValue(mapOf(symbol to dataPoint.close))
            portfolioHistory.add(dataPoint.date to currentValue)
        }

        strategy.finalize()

        // Calculate final value
        val finalPrice = data.last().close
        val finalValue = portfolio.totalValue(mapOf(symbol to finalPrice))

        // Calculate metrics
        val metrics = calculateMetrics(portfolioHistory, trades, initialBudget, finalValue)

        val executionTime = System.currentTimeMillis() - startTime

        println("\n" + "=".repeat(50))
        println("Simulation Complete")
        println("=".repeat(50))
        println("Final Portfolio Value: $${finalValue.setScale(2, RoundingMode.HALF_UP)}")
        println("Total Return: ${metrics.totalReturnPercent.setScale(2, RoundingMode.HALF_UP)}%")
        println("Total Trades: ${trades.size}")
        println("Execution Time: ${executionTime}ms")
        println("=".repeat(50))

        return SimulationResult(
            strategyName = strategy.name,
            symbol = symbol,
            startDate = data.first().date,
            endDate = data.last().date,
            initialBudget = initialBudget,
            finalBudget = finalValue,
            totalReturnPercent = metrics.totalReturnPercent,
            totalReturnAmount = metrics.totalReturnAmount,
            totalTrades = trades.size,
            winningTrades = metrics.winningTrades,
            losingTrades = metrics.losingTrades,
            maxDrawdown = metrics.maxDrawdown,
            sharpeRatio = metrics.sharpeRatio,
            dataPointsUsed = data.size,
            executionTimeMs = executionTime,
            trades = trades
        )
    }

    private fun calculateMetrics(
        portfolioHistory: List<Pair<LocalDate, BigDecimal>>,
        trades: List<Trade>,
        initialBudget: BigDecimal,
        finalValue: BigDecimal
    ): PerformanceMetrics {
        // Total return
        val totalReturnAmount = finalValue - initialBudget
        val totalReturnPercent = if (initialBudget > BigDecimal.ZERO) {
            (totalReturnAmount / initialBudget) * BigDecimal.valueOf(100)
        } else {
            BigDecimal.ZERO
        }

        // Max drawdown
        var peak = initialBudget
        var maxDrawdown = BigDecimal.ZERO
        portfolioHistory.forEach { (_, value) ->
            if (value > peak) {
                peak = value
            }
            val drawdown = ((peak - value) / peak) * BigDecimal.valueOf(100)
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown
            }
        }

        // Sharpe ratio (simplified - assumes risk-free rate = 0)
        val returns = portfolioHistory.zipWithNext { (_, v1), (_, v2) ->
            ((v2 - v1) / v1).toDouble()
        }

        val sharpeRatio = if (returns.isNotEmpty()) {
            val avgReturn = returns.average()
            val stdDev = sqrt(returns.map { (it - avgReturn) * (it - avgReturn) }.average())
            if (stdDev > 0) BigDecimal.valueOf(avgReturn / stdDev * sqrt(252.0)) // Annualized
            else BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }

        // Winning/losing trades
        val buyTrades = trades.filter { it.action == TradeAction.BUY }.associateBy { it.date }
        val sellTrades = trades.filter { it.action == TradeAction.SELL }

        var winningTrades = 0
        var losingTrades = 0

        sellTrades.forEach { sell ->
            // Find corresponding buy (simplified - assumes FIFO)
            val buy = buyTrades.values.firstOrNull()
            if (buy != null) {
                if (sell.price > buy.price) {
                    winningTrades++
                } else if (sell.price < buy.price) {
                    losingTrades++
                }
            }
        }

        return PerformanceMetrics(
            totalReturnPercent = totalReturnPercent,
            totalReturnAmount = totalReturnAmount,
            maxDrawdown = maxDrawdown,
            sharpeRatio = sharpeRatio,
            winningTrades = winningTrades,
            losingTrades = losingTrades
        )
    }
}

data class PerformanceMetrics(
    val totalReturnPercent: BigDecimal,
    val totalReturnAmount: BigDecimal,
    val maxDrawdown: BigDecimal,
    val sharpeRatio: BigDecimal,
    val winningTrades: Int,
    val losingTrades: Int
)

data class SimulationResult(
    val strategyName: String,
    val symbol: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val initialBudget: BigDecimal,
    val finalBudget: BigDecimal,
    val totalReturnPercent: BigDecimal,
    val totalReturnAmount: BigDecimal,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val maxDrawdown: BigDecimal,
    val sharpeRatio: BigDecimal,
    val dataPointsUsed: Int,
    val executionTimeMs: Long,
    val trades: List<Trade>
)
