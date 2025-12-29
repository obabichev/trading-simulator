package com.obabichev.trading

import com.obabichev.trading.schema.TradingStrategy
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseConnectionTest {

    companion object {
        private lateinit var connection: Connection
        private lateinit var dbUrl: String
        private lateinit var dbUsername: String
        private lateinit var dbPassword: String

        @BeforeAll
        @JvmStatic
        fun setup() {
            // Load test properties
            val props = Properties()
            val inputStream = DatabaseConnectionTest::class.java.classLoader
                .getResourceAsStream("test.properties")

            requireNotNull(inputStream) { "test.properties not found" }
            props.load(inputStream)

            // Get database connection details (environment variables override properties)
            dbUrl = System.getenv("DB_URL") ?: props.getProperty("db.url")
            dbUsername = System.getenv("DB_USERNAME") ?: props.getProperty("db.username")
            dbPassword = System.getenv("DB_PASSWORD") ?: props.getProperty("db.password")

            // Run Flyway migrations
            val flyway = Flyway.configure()
                .dataSource(dbUrl, dbUsername, dbPassword)
                .locations("classpath:db/migration")
                .cleanDisabled(false) // Allow clean for test database
                .load()

            // Clean and migrate for fresh test state
            flyway.clean()
            flyway.migrate()

            // Create database connection
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
            connection.autoCommit = true
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            if (::connection.isInitialized && !connection.isClosed) {
                connection.close()
            }
        }
    }

    @Test
    fun `should connect to database`() {
        assertNotNull(connection)
        assertTrue(connection.isValid(5))
    }

    @Test
    fun `should have migrated tables`() {
        val metadata = connection.metaData
        val tables = metadata.getTables(null, null, "%", arrayOf("TABLE"))

        val tableNames = mutableListOf<String>()
        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"))
        }

        assertTrue(tableNames.contains("trading_strategy"), "trading_strategy table should exist")
        assertTrue(tableNames.contains("market_data"), "market_data table should exist")
        assertTrue(tableNames.contains("flyway_schema_history"), "flyway_schema_history table should exist")
    }

    @Test
    fun `should insert and query data`() {
        // Insert test data using SQL
        connection.createStatement().use { statement ->
            statement.execute(
                """
                INSERT INTO trading_strategy (name, description)
                VALUES ('Test Strategy', 'A test strategy')
                """
            )
        }

        // Verify data was inserted
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(
                "SELECT id, name, description FROM trading_strategy WHERE name = 'Test Strategy'"
            )

            assertTrue(resultSet.next(), "Should have at least one result")
            assertEquals("Test Strategy", resultSet.getString("name"))
            assertEquals("A test strategy", resultSet.getString("description"))
        }

        // Cleanup
        connection.createStatement().use { statement ->
            statement.execute("DELETE FROM trading_strategy WHERE name = 'Test Strategy'")
        }
    }

    @Test
    fun `should verify Kodama table definition exists`() {
        // Just verify that the Kodama table object is accessible
        assertNotNull(TradingStrategy)
        assertEquals("trading_strategy", TradingStrategy.tableName)
        assertEquals("id", TradingStrategy.id.name)
        assertEquals("name", TradingStrategy.name.name)
        assertEquals("description", TradingStrategy.description.name)
    }
}
