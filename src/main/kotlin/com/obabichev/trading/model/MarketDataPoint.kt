package com.obabichev.trading.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Represents a single day of market data (OHLCV).
 */
data class MarketDataPoint(
    val date: LocalDate,
    val symbol: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val adjClose: BigDecimal,
    val volume: Long
) {
    /**
     * Typical price for the day (average of high, low, close).
     */
    val typical: BigDecimal
        get() = (high + low + close).divide(BigDecimal.valueOf(3), 8, BigDecimal.ROUND_HALF_UP)
}
