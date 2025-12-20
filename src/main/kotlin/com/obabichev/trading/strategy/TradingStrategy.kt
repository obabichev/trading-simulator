package com.obabichev.trading.strategy

import com.obabichev.trading.model.MarketDataPoint
import com.obabichev.trading.model.Portfolio
import com.obabichev.trading.model.Trade

/**
 * Interface for trading strategies.
 *
 * A trading strategy observes market data and decides when to buy or sell.
 */
interface TradingStrategy {
    /**
     * Name of the strategy (e.g., "Buy and Hold", "Moving Average Crossover").
     */
    val name: String

    /**
     * Called once before simulation starts.
     * Can be used to initialize state.
     */
    fun initialize() {}

    /**
     * Called on each trading day with current market data and portfolio state.
     *
     * @param dataPoint Current day's market data
     * @param portfolio Current portfolio state
     * @param historicalData Historical data up to current day (for indicators)
     * @return Trade to execute, or null if no action
     */
    fun onData(
        dataPoint: MarketDataPoint,
        portfolio: Portfolio,
        historicalData: List<MarketDataPoint>
    ): Trade?

    /**
     * Called once after simulation ends.
     * Can be used for cleanup or final calculations.
     */
    fun finalize() {}
}
