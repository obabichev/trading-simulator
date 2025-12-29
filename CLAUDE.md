# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Trading simulator built with Kotlin and Gradle. Uses Kotlin JVM 2.2.21, targeting Java 17.

**Package:** `com.obabichev.trading`
**Version:** `1.0-SNAPSHOT`

## Database Setup

This project uses PostgreSQL for data persistence. Start the database with Docker Compose:

```bash
# Start PostgreSQL database
docker-compose up -d

# Stop database
docker-compose down

# Stop and remove data
docker-compose down -v
```

**Connection Details:**
- Host: `localhost:5455`
- Database: `trading_dev`
- Username: `trading_user`
- Password: `trading_password`

**Database Migrations:**
- Uses Flyway for schema management
- Migrations located in `src/main/resources/db/migration/`
- Tests automatically run migrations on startup

## Build Commands

```bash
# Build and test
./gradlew build                # Full build with tests
./gradlew test                 # Run all tests
./gradlew test --tests "ClassName.methodName"  # Run specific test

# Development
./gradlew assemble             # Compile without tests
./gradlew clean build          # Clean build
./gradlew classes              # Compile main sources only
./gradlew compileKotlin        # Compile Kotlin sources
./gradlew compileTestKotlin    # Compile test sources

# Verification
./gradlew check                # Run all checks including tests

# Data Management
./gradlew run                  # Generate sample market data in tdata/ directory
```

## Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   ├── Main.kt                      # Application entry point
│   │   ├── data/
│   │   │   ├── SampleDataGenerator.kt   # Generate test market data
│   │   │   └── YahooFinanceDownloader.kt # Yahoo Finance downloader (needs auth)
│   │   └── schema/Tables.kt             # Kodama table definitions
│   └── resources/
│       └── db/migration/                # Flyway database migrations
├── test/
│   ├── kotlin/                          # Test sources (JUnit Platform)
│   └── resources/
│       └── test.properties              # Test database configuration
└── tdata/                               # Historical market data (CSV files)
    ├── README.md                        # Data sourcing instructions
    └── *.csv                            # Symbol data (AAPL.csv, TSLA.csv, etc.)
```

## Historical Market Data

### Sample Data (for development/testing)

Generate sample market data with realistic price movements:
```bash
./gradlew run
```

This creates CSV files in `tdata/` for 8 symbols (AAPL, TSLA, MSFT, GOOGL, AMZN, NVDA, META, SPY) covering the last 2 years.

### Real Data (for production)

For real historical data, manually download CSV files from Yahoo Finance:
1. Go to https://finance.yahoo.com
2. Search for a symbol (e.g., "AAPL")
3. Click "Historical Data" tab
4. Set date range
5. Click "Download"
6. Save to `tdata/` directory

See `tdata/README.md` for detailed instructions.

### CSV Format

All data files follow Yahoo Finance format:
```csv
Date,Open,High,Low,Close,Adj Close,Volume
2023-12-21,195.18,196.95,195.09,196.75,196.13,52242800
2023-12-22,196.97,197.19,194.84,195.71,195.09,41728400
```

## Database Library (Kodama)

This project uses Kodama (v0.2.0), a type-safe SQL query builder for Kotlin and PostgreSQL.

### Setup and Configuration

- **Installation:** Kodama is installed from Maven Local (`~/.m2/repository/com/obabichev/kodama/`)
- **Compiler Plugin:** Enabled in `build.gradle.kts` with `id("com.obabichev.kodama") version "0.2.0"`
- **Table Definitions:** Located in `src/main/kotlin/com/obabichev/trading/schema/Tables.kt`
- **Generated Code:** Auto-generated in `build/generated/kodama/com/obabichev/trading/schema/generated/`
- **Required Compiler Flag:** `-Xcontext-parameters` (for Entity Layer support)

### Usage Patterns

**Creating a transaction:**
```kotlin
val transaction = JdbcTransaction(dbUrl, dbUsername, dbPassword)
try {
    // Use transaction for queries and inserts
    transaction.commit()
} finally {
    transaction.close()
}
```

**INSERT operations:**
```kotlin
val result = TradingStrategy.insert(
    transaction = transaction,
    id = 1,
    name = "Strategy Name",
    description = "Description"
)
println("Rows affected: ${result.rowsAffected}")
```

**Query with single column SELECT:**
```kotlin
val names = query()
    .from(TradingStrategy)
    .select { tradingStrategy.name }
    .where { tradingStrategy.id eq 100 }
    .execute(transaction)
    .map { it.tradingStrategy.name }  // Eagerly extract data during iteration
    .toList()
```

**Query with multiple column SELECT:**
```kotlin
val results = query()
    .from(MarketData)
    .select { marketData.symbol }
    .select { marketData.openPrice }
    .select { marketData.closePrice }
    .where { marketData.symbol eq "AAPL" }
    .execute(transaction)
    .map { result ->
        Triple(
            result.marketData.symbol,
            result.marketData.openPrice,
            result.marketData.closePrice
        )
    }
    .toList()
```

**Query with selectAll:**
```kotlin
val strategies = query()
    .from(TradingStrategy)
    .selectAll(TradingStrategy)
    .where { tradingStrategy.id eq 100 }
    .execute(transaction)
    .map { result ->
        Triple(
            result.tradingStrategy.id,
            result.tradingStrategy.name,
            result.tradingStrategy.description
        )
    }
    .toList()
```

### Important Notes

**QueryResultIterable Behavior:**
- Results are lazy and read from ResultSet on property access
- Always use `.map { }` to extract data during iteration, then call `.toList()`
- Never call `.toList()` first, then access properties (ResultSet will be exhausted)

### Integration Tests

See `src/test/kotlin/com/obabichev/trading/KodamaIntegrationTest.kt` for working examples of:
- Type-safe INSERT operations
- Single-column SELECT queries
- **Multi-column SELECT queries** (all column accessors working)
- WHERE clause filtering
- selectAll() queries
- Type-safe column selection
- JdbcTransaction usage patterns

**Test Status:** 7/7 passing ✅

## Code Standards

- Kotlin code style: Official (configured in gradle.properties)
- Test framework: JUnit Platform
- Tests should mirror main source package structure in `src/test/kotlin/`
- Database: PostgreSQL with Flyway migrations
