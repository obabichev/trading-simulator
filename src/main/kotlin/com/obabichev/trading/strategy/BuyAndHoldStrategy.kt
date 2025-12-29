package com.obabichev.trading.strategy

import com.obabichev.trading.model.MarketDataPoint
import com.obabichev.trading.model.Portfolio
import com.obabichev.trading.model.Trade
import com.obabichev.trading.model.TradeAction
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Simple Buy and Hold strategy.
 *
 * Buys as many shares as possible on the first day and holds until the end.
 */
class BuyAndHoldStrategy : TradingStrategy {
    override val name: String = "Buy and Hold"

    private var hasBought = false

    override fun initialize() {
        hasBought = false
    }

    override fun onData(
        dataPoint: MarketDataPoint,
        portfolio: Portfolio,
        historicalData: List<MarketDataPoint>
    ): Trade? {
        // Buy on first day if we haven't bought yet
        if (!hasBought && !portfolio.hasPosition(dataPoint.symbol)) {
            hasBought = true

            // Calculate how many shares we can buy with all our cash
            val sharesToBuy = portfolio.cash
                .divide(dataPoint.close, 8, RoundingMode.DOWN)

            if (sharesToBuy > BigDecimal.ZERO) {
                return Trade(
                    date = dataPoint.date,
                    action = TradeAction.BUY,
                    symbol = dataPoint.symbol,
                    shares = sharesToBuy,
                    price = dataPoint.close,
                    notes = "Initial buy - Buy and Hold strategy"
                )
            }
        }

        // Hold - no action
        return null
    }
}
