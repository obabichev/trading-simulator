package com.obabichev.trading.schema

import com.obabichev.kodama.schema.Table
import com.obabichev.kodama.schema.primaryKey
import java.time.LocalDate
import java.time.LocalDateTime

object TradingStrategy : Table("trading_strategy") {
    val id = serial("id").primaryKey()
    val name = varchar("name", 255)
    val description = varchar("description", 1000)
}

object MarketData : Table("market_data") {
    val id = serial("id").primaryKey()
    val symbol = varchar("symbol", 50)
    val openPrice = decimal("open_price", 18, 8)
    val closePrice = decimal("close_price", 18, 8)
    val highPrice = decimal("high_price", 18, 8)
    val lowPrice = decimal("low_price", 18, 8)
    val volume = decimal("volume", 18, 8)
}

object SimulationRun : Table("simulation_run") {
    val id = serial("id").primaryKey()
    val strategyName = varchar("strategy_name", 255)
    val symbol = varchar("symbol", 50)

    val startDate = date("start_date")
    val endDate = date("end_date")

    val initialBudget = decimal("initial_budget", 18, 2)
    val finalBudget = decimal("final_budget", 18, 2)
    val totalReturn = decimal("total_return", 18, 4)
    val totalReturnAmount = decimal("total_return_amount", 18, 2)

    val totalTrades = integer("total_trades")
    val winningTrades = integer("winning_trades")
    val losingTrades = integer("losing_trades")

    val maxDrawdown = decimal("max_drawdown", 18, 4)
    val sharpeRatio = decimal("sharpe_ratio", 18, 4)

    val dataPointsUsed = integer("data_points_used")
    val executedAt = timestamp("executed_at")
    val executionTimeMs = integer("execution_time_ms")

    val notes = varchar("notes", 5000)
}

object SimulationTrade : Table("simulation_trade") {
    val id = serial("id").primaryKey()
    val simulationRunId = integer("simulation_run_id")

    val tradeDate = date("trade_date")
    val action = varchar("action", 10)

    val shares = decimal("shares", 18, 8)
    val price = decimal("price", 18, 8)
    val totalAmount = decimal("total_amount", 18, 2)

    val portfolioValue = decimal("portfolio_value", 18, 2)
    val notes = varchar("notes", 1000)
}
