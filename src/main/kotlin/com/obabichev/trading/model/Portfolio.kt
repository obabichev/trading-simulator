package com.obabichev.trading.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Represents a trading portfolio with cash and stock positions.
 */
data class Portfolio(
    val cash: BigDecimal,
    val positions: Map<String, BigDecimal> = emptyMap()  // symbol -> shares
) {
    /**
     * Calculates total portfolio value given current market prices.
     */
    fun totalValue(prices: Map<String, BigDecimal>): BigDecimal {
        val stockValue = positions.entries.sumOf { (symbol, shares) ->
            val price = prices[symbol] ?: BigDecimal.ZERO
            shares * price
        }
        return cash + stockValue
    }

    /**
     * Executes a buy order.
     */
    fun buy(symbol: String, shares: BigDecimal, price: BigDecimal): Portfolio {
        val cost = shares * price
        require(cash >= cost) { "Insufficient funds: have $cash, need $cost" }

        val newShares = (positions[symbol] ?: BigDecimal.ZERO) + shares
        return copy(
            cash = cash - cost,
            positions = positions + (symbol to newShares)
        )
    }

    /**
     * Executes a sell order.
     */
    fun sell(symbol: String, shares: BigDecimal, price: BigDecimal): Portfolio {
        val currentShares = positions[symbol] ?: BigDecimal.ZERO
        require(currentShares >= shares) { "Insufficient shares: have $currentShares, trying to sell $shares" }

        val proceeds = shares * price
        val newShares = currentShares - shares
        val newPositions = if (newShares == BigDecimal.ZERO) {
            positions - symbol
        } else {
            positions + (symbol to newShares)
        }

        return copy(
            cash = cash + proceeds,
            positions = newPositions
        )
    }

    /**
     * Gets the number of shares held for a symbol.
     */
    fun sharesOf(symbol: String): BigDecimal = positions[symbol] ?: BigDecimal.ZERO

    /**
     * Checks if the portfolio has any shares of a symbol.
     */
    fun hasPosition(symbol: String): Boolean = positions.containsKey(symbol)
}
