package com.obabichev.trading.persistence

import com.obabichev.kodama.execute.JdbcTransaction
import com.obabichev.trading.schema.SimulationRun
import com.obabichev.trading.schema.SimulationTrade
import com.obabichev.trading.schema.generated.insert
import com.obabichev.trading.simulation.SimulationResult
import java.time.LocalDateTime

/**
 * Saves simulation results to the database using Kodama's type-safe DSL.
 */
object SimulationResultPersistence {

    /**
     * Saves a simulation run and its trades to the database.
     *
     * @return The ID of the saved simulation run
     */
    fun save(result: SimulationResult, transaction: JdbcTransaction): Int {
        println("\nSaving simulation results to database...")

        // Insert simulation run using Kodama's generated insert method
        // Note: id parameter is automatically excluded because we use serial()
        val insertResult = SimulationRun.insert(
            transaction = transaction,
            strategyName = result.strategyName,
            symbol = result.symbol,
            startDate = result.startDate,
            endDate = result.endDate,
            initialBudget = result.initialBudget,
            finalBudget = result.finalBudget,
            totalReturn = result.totalReturnPercent,
            totalReturnAmount = result.totalReturnAmount,
            totalTrades = result.totalTrades,
            winningTrades = result.winningTrades,
            losingTrades = result.losingTrades,
            maxDrawdown = result.maxDrawdown,
            sharpeRatio = result.sharpeRatio,
            dataPointsUsed = result.dataPointsUsed,
            executedAt = LocalDateTime.now(),
            executionTimeMs = result.executionTimeMs.toInt(),
            notes = "Simulation run completed successfully"
        )

        val runId = insertResult.generatedKeys["id"] as? Int
            ?: throw IllegalStateException("Failed to get generated ID for simulation run")

        transaction.commit()
        println("  ✅ Simulation run saved to database (ID: $runId)")

        // Save trades using Kodama's generated insert method
        if (result.trades.isNotEmpty()) {
            result.trades.forEach { trade ->
                SimulationTrade.insert(
                    transaction = transaction,
                    simulationRunId = runId,
                    tradeDate = trade.date,
                    action = trade.action.name,
                    shares = trade.shares,
                    price = trade.price,
                    totalAmount = trade.totalAmount,
                    portfolioValue = result.finalBudget,
                    notes = trade.notes ?: ""
                )
            }

            transaction.commit()
            println("  ✅ ${result.trades.size} trades saved to database")
        }

        println("Simulation results saved successfully!")

        return runId
    }
}
