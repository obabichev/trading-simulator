package com.obabichev.trading

import com.obabichev.kodama.execute.JdbcTransaction
import com.obabichev.trading.persistence.SimulationResultPersistence
import com.obabichev.trading.simulation.SimulationEngine
import com.obabichev.trading.strategy.BuyAndHoldStrategy
import org.flywaydb.core.Flyway
import java.io.File
import java.math.BigDecimal
import java.util.Properties

fun main() {
    println("""

        ╔═══════════════════════════════════════════╗
        ║   Trading Strategy Simulator              ║
        ║   Powered by Kodama                       ║
        ╚═══════════════════════════════════════════╝

    """.trimIndent())

    // Load database configuration
    val props = Properties()
    val inputStream = object {}.javaClass.classLoader
        .getResourceAsStream("test.properties")

    requireNotNull(inputStream) { "test.properties not found" }
    props.load(inputStream)

    val dbUrl = System.getenv("DB_URL") ?: props.getProperty("db.url")
    val dbUsername = System.getenv("DB_USERNAME") ?: props.getProperty("db.username")
    val dbPassword = System.getenv("DB_PASSWORD") ?: props.getProperty("db.password")

    // Run Flyway migrations
    println("Running database migrations...")
    val flyway = Flyway.configure()
        .dataSource(dbUrl, dbUsername, dbPassword)
        .locations("classpath:db/migration")
        .load()

    val pendingMigrations = flyway.info().pending().size
    if (pendingMigrations > 0) {
        println("  Found $pendingMigrations pending migration(s)")
        flyway.migrate()
        println("  ✅ Migrations completed")
    } else {
        println("  ✅ Database is up to date")
    }

    // Check if sample data exists
    val dataDir = File("tdata")
    if (!dataDir.exists() || dataDir.listFiles { file -> file.extension == "csv" }?.isEmpty() == true) {
        println("\n⚠️  No market data found in tdata/ directory")
        println("Run './gradlew run' to generate sample data, or download real data from Yahoo Finance")
        return
    }

    // Run simulation
    val strategy = BuyAndHoldStrategy()
    val engine = SimulationEngine(
        strategy = strategy,
        initialBudget = BigDecimal("10000.00")
    )

    val result = engine.runSimulation(
        symbol = "AAPL"
    )

    // Save results to database
    val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
    try {
        val runId = SimulationResultPersistence.save(result, transaction)
        println("\n✅ Simulation run ID: $runId")
    } finally {
        transaction.close()
    }

    println("\n" + "=".repeat(50))
    println("Simulation Complete!")
    println("Check the database for full results.")
    println("=".repeat(50))
}
