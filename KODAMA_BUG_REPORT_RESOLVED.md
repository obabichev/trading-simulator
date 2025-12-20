# Kodama Bug Report: Generated Insert Methods Not Available in Main Source Set

## ✅ RESOLVED IN KODAMA 0.3.0

This issue was **fixed by the Kodama team**. Generated insert methods are now properly available in main sources. See `KODAMA_0.3.0_SUCCESS.md` for details on the working implementation.

---

## Original Report (Kodama 0.2.0)

## Environment
- **Kodama Version**: 0.2.0
- **Kotlin Version**: 2.2.21
- **Gradle Version**: 9.0.0
- **Database**: PostgreSQL 16
- **Java Toolchain**: 17

## Issue Summary

Kodama successfully generates insert methods and other extension functions, but these generated methods are **not available during compilation of the main source set**. The generated code exists in `build/generated/kodama/` but the Kotlin compiler cannot resolve references to it when compiling main sources.

## Steps to Reproduce

### 1. Define Tables with Date/Time Types

```kotlin
// src/main/kotlin/com/obabichev/trading/schema/Tables.kt
package com.obabichev.trading.schema

import com.obabichev.kodama.schema.Table
import java.time.LocalDate
import java.time.LocalDateTime

object SimulationRun : Table("simulation_run") {
    val id = integer("id")
    val strategyName = varchar("strategy_name", 255)
    val symbol = varchar("symbol", 50)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val initialBudget = decimal("initial_budget", 18, 2)
    val finalBudget = decimal("final_budget", 18, 2)
    val totalReturn = decimal("total_return", 18, 4)
    val totalReturnAmount = decimal("total_return_amount", 18, 2)
    val totalTrades = integer("total_trades")
    val winningTrades = integer("winning_trades")
    val losingTrades = integer("losing_trades")
    val maxDrawdown = decimal("max_drawdown", 18, 4)
    val sharpeRatio = decimal("sharpe_ratio", 18, 4)
    val dataPointsUsed = integer("data_points_used")
    val executedAt = timestamp("executed_at")
    val executionTimeMs = integer("execution_time_ms")
    val notes = varchar("notes", 5000)
}

object SimulationTrade : Table("simulation_trade") {
    val id = integer("id")
    val simulationRunId = integer("simulation_run_id")
    val tradeDate = date("trade_date")
    val action = varchar("action", 10)
    val shares = decimal("shares", 18, 8)
    val price = decimal("price", 18, 8)
    val totalAmount = decimal("total_amount", 18, 2)
    val portfolioValue = decimal("portfolio_value", 18, 2)
    val notes = varchar("notes", 1000)
}
```

### 2. Try to Use Generated Insert Methods in Main Source

```kotlin
// src/main/kotlin/com/obabichev/trading/persistence/SimulationResultPersistence.kt
package com.obabichev.trading.persistence

import com.obabichev.kodama.execute.JdbcTransaction
import com.obabichev.trading.schema.SimulationRun
import com.obabichev.trading.schema.SimulationTrade
import com.obabichev.trading.schema.generated.insert  // ❌ Unresolved reference 'generated'
import java.time.LocalDateTime

object SimulationResultPersistence {
    fun save(transaction: JdbcTransaction) {
        // ❌ Unresolved reference 'insert'
        val insertResult = SimulationRun.insert(
            transaction = transaction,
            id = 0,
            strategyName = "Buy and Hold",
            symbol = "AAPL",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            initialBudget = BigDecimal("10000"),
            finalBudget = BigDecimal("10000"),
            totalReturn = BigDecimal.ZERO,
            totalReturnAmount = BigDecimal.ZERO,
            totalTrades = 0,
            winningTrades = 0,
            losingTrades = 0,
            maxDrawdown = BigDecimal.ZERO,
            sharpeRatio = BigDecimal.ZERO,
            dataPointsUsed = 0,
            executedAt = LocalDateTime.now(),
            executionTimeMs = 0,
            notes = "Test"
        )
    }
}
```

### 3. Build the Project

```bash
./gradlew clean build
```

## Actual Result

**Compilation fails with:**

```
e: Unresolved reference 'generated'.
e: Unresolved reference 'insert'.

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileKotlin'.
> Compilation error. See log for more details
```

**But the code generation succeeds:**

```
> Task :generateKodamaExtensions
Kodama: Generated 4 tables, 2 query combinations, 6 column patterns, 0 selection patterns, 0 entity bindings
```

## Investigation

### Generated Code Exists

The insert methods ARE generated and exist in the correct location:

```bash
$ find build/generated/kodama -name "*.kt" -type f
build/generated/kodama/com/obabichev/trading/schema/generated/QueryExtensions.kt

$ grep -n "fun.*insert" build/generated/kodama/com/obabichev/trading/schema/generated/QueryExtensions.kt
1490:fun com.obabichev.trading.schema.TradingStrategy.insert(
1518:fun com.obabichev.trading.schema.MarketData.insert(
1561:fun com.obabichev.trading.schema.SimulationRun.insert(
1606:fun com.obabichev.trading.schema.SimulationTrade.insert(
```

### Generated Insert Method Signature

```kotlin
fun com.obabichev.trading.schema.SimulationRun.insert(
    transaction: com.obabichev.kodama.execute.JdbcTransaction,
    id: Int,
    strategyName: String,
    symbol: String,
    startDate: java.time.LocalDate,
    endDate: java.time.LocalDate,
    initialBudget: java.math.BigDecimal,
    finalBudget: java.math.BigDecimal,
    totalReturn: java.math.BigDecimal,
    totalReturnAmount: java.math.BigDecimal,
    totalTrades: Int,
    winningTrades: Int,
    losingTrades: Int,
    maxDrawdown: java.math.BigDecimal,
    sharpeRatio: java.math.BigDecimal,
    dataPointsUsed: Int,
    executedAt: java.time.LocalDateTime,
    executionTimeMs: Int,
    notes: String
): com.obabichev.kodama.insert.InsertResult {
    val table = this
    val insert = com.obabichev.kodama.insert.InsertStatement(
        table = table,
        columns = listOf(table.id, table.strategyName, table.symbol, table.startDate, table.endDate, table.initialBudget, table.finalBudget, table.totalReturn, table.totalReturnAmount, table.totalTrades, table.winningTrades, table.losingTrades, table.maxDrawdown, table.sharpeRatio, table.dataPointsUsed, table.executedAt, table.executionTimeMs, table.notes),
        values = listOf(id, strategyName, symbol, startDate, endDate, initialBudget, finalBudget, totalReturn, totalReturnAmount, totalTrades, winningTrades, losingTrades, maxDrawdown, sharpeRatio, dataPointsUsed, executedAt, executionTimeMs, notes)
    )
    return transaction.executeInsert(insert)
}
```

This looks correct! The method signature matches what we're trying to use.

### Source Set Configuration Issue

The problem appears to be that:
1. `:generateKodamaExtensions` task runs successfully
2. Generated code is placed in `build/generated/kodama/`
3. BUT the `:compileKotlin` task doesn't see this generated code in its source path

The generated code directory is not being added to the Kotlin source sets before compilation.

## Build Configuration

### build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.21"
    id("com.obabichev.kodama") version "0.2.0"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.obabichev.kodama:kodama-core:0.2.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")  // Required for Kodama Entity Layer
    }
}
```

No explicit Kodama configuration is provided - relying on auto-detection.

## Expected Behavior

The generated insert methods should be available for use in main source files after the `:generateKodamaExtensions` task completes and before the `:compileKotlin` task runs.

## Workaround

Currently using raw SQL with JDBC prepared statements instead of Kodama's type-safe insert methods:

```kotlin
val insertSql = """
    INSERT INTO simulation_run (
        strategy_name, symbol, start_date, end_date,
        initial_budget, final_budget, total_return, total_return_amount,
        total_trades, winning_trades, losing_trades,
        max_drawdown, sharpe_ratio, data_points_used,
        executed_at, execution_time_ms, notes
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    RETURNING id
""".trimIndent()

val connection = transaction.connection
val stmt = connection.prepareStatement(insertSql)
stmt.setString(1, result.strategyName)
stmt.setDate(3, java.sql.Date.valueOf(result.startDate))
// ... set all parameters
val rs = stmt.executeQuery()
```

This defeats the purpose of using Kodama, as we lose:
- Type safety
- Compile-time validation
- IDE auto-completion
- Refactoring safety

## Possible Root Cause

The Kodama Gradle plugin may not be:
1. Adding the generated source directory to the Kotlin source set before `compileKotlin`
2. Setting up proper task dependencies (`:compileKotlin` should depend on `:generateKodamaExtensions`)
3. Registering the generated sources with the Kotlin compiler

## Suggested Fix

The Kodama Gradle plugin should automatically:

```kotlin
// In KodamaPlugin.kt
kotlin.sourceSets.named("main") {
    kotlin.srcDir(generatedSourceDir)  // Add generated code to main source set
}

// Ensure proper task dependencies
tasks.named("compileKotlin") {
    dependsOn("generateKodamaExtensions")
}
```

## Additional Context

- The project structure is standard Gradle/Kotlin
- No custom source set configuration
- The issue affects all generated extension functions (insert, select, etc.)
- Date/time column types (`date()`, `timestamp()`) work correctly in table definitions
- Query building works fine in test sources (suggesting generated code IS available there)

## Impact

**Critical**: Without access to generated insert methods in main sources, Kodama cannot be used for its primary purpose - type-safe database operations. Users are forced to fall back to raw SQL, losing all benefits of using Kodama.

## Test Case

A minimal reproducible test case is available at:
- Repository: `/Users/Oleg.Babichev/dev/trading-simulator`
- Relevant files:
  - `src/main/kotlin/com/obabichev/trading/schema/Tables.kt`
  - `src/main/kotlin/com/obabichev/trading/persistence/SimulationResultPersistence.kt`
  - `build.gradle.kts`

## Related Issues

This may be related to how Kotlin compiler plugins and Gradle source sets interact in Kotlin 2.x. Similar issues have been reported with other code generation tools when migrating to newer Kotlin versions.
