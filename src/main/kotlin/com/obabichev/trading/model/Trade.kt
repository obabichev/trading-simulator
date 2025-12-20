package com.obabichev.trading.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Represents a buy or sell trade action.
 */
data class Trade(
    val date: LocalDate,
    val action: TradeAction,
    val symbol: String,
    val shares: BigDecimal,
    val price: BigDecimal,
    val notes: String? = null
) {
    val totalAmount: BigDecimal
        get() = shares * price
}

enum class TradeAction {
    BUY,
    SELL
}
