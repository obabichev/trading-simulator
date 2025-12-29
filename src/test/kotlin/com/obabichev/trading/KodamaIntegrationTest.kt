package com.obabichev.trading

import com.obabichev.trading.schema.TradingStrategy
import com.obabichev.trading.schema.MarketData
import com.obabichev.trading.schema.generated.*
import com.obabichev.kodama.query.eq
import com.obabichev.kodama.execute.JdbcTransaction
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for Kodama 0.3.0 with serial columns and updated query API.
 * Tests that code generation properly handles auto-increment primary keys and query DSL.
 */
class KodamaIntegrationTest {

    companion object {
        private lateinit var dbUrl: String
        private lateinit var dbUsername: String
        private lateinit var dbPassword: String

        @BeforeAll
        @JvmStatic
        fun setup() {
            val props = Properties()
            val inputStream = KodamaIntegrationTest::class.java.classLoader
                .getResourceAsStream("test.properties")

            requireNotNull(inputStream) { "test.properties not found" }
            props.load(inputStream)

            dbUrl = System.getenv("DB_URL") ?: props.getProperty("db.url")
            dbUsername = System.getenv("DB_USERNAME") ?: props.getProperty("db.username")
            dbPassword = System.getenv("DB_PASSWORD") ?: props.getProperty("db.password")

            val flyway = Flyway.configure()
                .dataSource(dbUrl, dbUsername, dbPassword)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load()

            flyway.clean()
            flyway.migrate()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            // Cleanup if needed
        }
    }

    @Test
    fun `should insert using Kodama generated insert method with serial column`() {
        println("Testing Kodama INSERT with serial column (auto-increment)...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            val result = TradingStrategy.insert(
                transaction = transaction,
                name = "Moving Average Crossover",
                description = "Buy when short MA crosses above long MA"
            )

            transaction.commit()

            println("✅ INSERT succeeded!")
            println("   Rows affected: ${result.rowsAffected}")
            println("   Generated ID: ${result.generatedKeys["id"]}")

            assertTrue(result.rowsAffected > 0, "Should insert at least one row")
            assertNotNull(result.generatedKeys["id"], "Should have generated ID")
            assertTrue((result.generatedKeys["id"] as Int) > 0, "Generated ID should be positive")
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should query using Kodama DSL with selectAll`() {
        println("Testing Kodama QUERY DSL with selectAll...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            // Insert test data using Kodama's insert method
            TradingStrategy.insert(
                transaction = transaction,
                name = "Test Strategy",
                description = "Test description"
            )
            transaction.commit()

            val results = from(TradingStrategy)
                .selectAll(TradingStrategy)
                .execute(transaction)

            var count = 0
            var foundTestStrategy = false

            results.forEach { row ->
                count++
                if (row.tradingStrategy.name == "Test Strategy") {
                    foundTestStrategy = true
                    println("   Found: ID=${row.tradingStrategy.id}, Name=${row.tradingStrategy.name}")
                    assertEquals("Test description", row.tradingStrategy.description)
                    assertTrue(row.tradingStrategy.id > 0, "ID should be auto-generated")
                }
            }

            println("✅ QUERY succeeded!")
            println("   Number of results: $count")

            assertTrue(count > 0, "Should have at least one result")
            assertTrue(foundTestStrategy, "Should find 'Test Strategy'")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should query with WHERE clause`() {
        println("Testing Kodama WHERE clause...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            // Insert test data
            TradingStrategy.insert(transaction, "Strategy A", "Description A")
            TradingStrategy.insert(transaction, "Strategy B", "Description B")
            TradingStrategy.insert(transaction, "Strategy C", "Description C")
            transaction.commit()

            val results = from(TradingStrategy)
                .selectAll(TradingStrategy)
                .where { tradingStrategy.name eq "Strategy B" }
                .execute(transaction)

            var count = 0
            var foundStrategy: Pair<Int, String>? = null

            results.forEach { row ->
                count++
                foundStrategy = Pair(row.tradingStrategy.id, row.tradingStrategy.name)
            }

            println("✅ WHERE clause query succeeded!")
            println("   Found $count result(s)")

            assertEquals(1, count, "Should find exactly one strategy")
            assertNotNull(foundStrategy, "Should have found strategy")
            assertEquals("Strategy B", foundStrategy!!.second)
            assertTrue(foundStrategy!!.first > 0, "ID should be auto-generated")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should work with multiple PascalCase tables`() {
        println("Testing multiple PascalCase tables...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            // Insert into MarketData using raw SQL (MarketData doesn't have insert method yet in our tests)
            transaction.executeUpdate(
                """
                INSERT INTO market_data (symbol, timestamp, open_price, close_price, high_price, low_price, volume)
                VALUES ('AAPL', '2024-01-01 10:00:00', 150.00, 152.50, 153.00, 149.50, 1000000.00)
                """
            )
            transaction.commit()

            // Query MarketData
            val marketResults = from(MarketData)
                .selectAll(MarketData)
                .where { marketData.symbol eq "AAPL" }
                .execute(transaction)

            var count = 0
            marketResults.forEach { row ->
                count++
                assertEquals("AAPL", row.marketData.symbol)
                println("   Symbol: ${row.marketData.symbol}, Close: ${row.marketData.closePrice}")
            }

            println("✅ MarketData query succeeded!")
            assertEquals(1, count, "Should find exactly one result")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should handle selectAll with type safety`() {
        println("Testing selectAll with type safety...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            val insertResult = TradingStrategy.insert(
                transaction = transaction,
                name = "All Columns Strategy",
                description = "Testing selectAll"
            )
            transaction.commit()

            val insertedId = insertResult.generatedKeys["id"] as Int

            val results = from(TradingStrategy)
                .selectAll(TradingStrategy)
                .where { tradingStrategy.id eq insertedId }
                .execute(transaction)

            var count = 0
            results.forEach { row ->
                count++

                // With selectAll, all columns should be accessible
                assertEquals(insertedId, row.tradingStrategy.id)
                assertEquals("All Columns Strategy", row.tradingStrategy.name)
                assertEquals("Testing selectAll", row.tradingStrategy.description)
            }

            println("✅ selectAll succeeded!")
            assertEquals(1, count, "Should find exactly one result")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should verify serial column generates incrementing IDs`() {
        println("Testing serial column ID generation...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            // Insert multiple rows
            val result1 = TradingStrategy.insert(transaction, "Strategy 1", "First")
            val result2 = TradingStrategy.insert(transaction, "Strategy 2", "Second")
            val result3 = TradingStrategy.insert(transaction, "Strategy 3", "Third")
            transaction.commit()

            val id1 = result1.generatedKeys["id"] as Int
            val id2 = result2.generatedKeys["id"] as Int
            val id3 = result3.generatedKeys["id"] as Int

            println("   Generated IDs: $id1, $id2, $id3")

            // Verify IDs are sequential
            assertTrue(id1 > 0, "First ID should be positive")
            assertTrue(id2 > id1, "Second ID should be greater than first")
            assertTrue(id3 > id2, "Third ID should be greater than second")

            println("✅ Serial column generates incrementing IDs correctly!")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }

    @Test
    fun `should handle queries with ORDER BY`() {
        println("Testing ORDER BY clause...")

        val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
        try {
            // Insert test data in random order
            TradingStrategy.insert(transaction, "Zebra Strategy", "Z")
            TradingStrategy.insert(transaction, "Alpha Strategy", "A")
            TradingStrategy.insert(transaction, "Beta Strategy", "B")
            transaction.commit()

            // Query with ORDER BY name ascending
            val results = from(TradingStrategy)
                .selectAll(TradingStrategy)
                .orderBy { tradingStrategy.name.asc() }
                .execute(transaction)

            val names = mutableListOf<String>()
            results.forEach { row ->
                names.add(row.tradingStrategy.name)
            }

            println("✅ ORDER BY succeeded!")
            println("   Ordered names: ${names.joinToString(", ")}")

            assertTrue(names.size >= 3, "Should have at least 3 results")
            // Find our test strategies in the results
            val testStrategies = names.filter { it.contains("Strategy") && (it.startsWith("Alpha") || it.startsWith("Beta") || it.startsWith("Zebra")) }
            assertTrue(testStrategies.isNotEmpty(), "Should find our test strategies")

            transaction.rollback()
        } finally {
            transaction.close()
        }
    }
}
