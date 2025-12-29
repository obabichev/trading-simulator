# Kodama Limitations Report

## ✅ ALL ISSUES RESOLVED IN KODAMA 0.3.0!

Both major issues documented in this file have been **completely resolved** by the Kodama team in version 0.3.0:

1. ✅ **Generated insert methods** are now available in main sources
2. ✅ **Serial columns** (`serial()`, `bigserial()`, `smallserial()`) fully supported with auto-generated IDs

See `KODAMA_0.3.0_SUCCESS.md` for the successful implementation.

---

## Original Report (Kodama v0.2.0)

This document describes limitations encountered while using Kodama v0.2.0 in the trading-simulator project.

## Project Context
- **Kodama Version**: 0.2.0 → **Updated to 0.3.0** ✅
- **Kotlin Version**: 2.2.21
- **Database**: PostgreSQL 16
- **Build Tool**: Gradle 9.0.0

---

## ~~CRITICAL Limitation: Generated Insert Methods Not Available in Main Source Set~~

### Status: ✅ RESOLVED IN 0.3.0

### Issue Description
Kodama successfully generates insert methods and other extension functions during the `:generateKodamaExtensions` task, but these generated methods are **not available for use in the main source set** during compilation. The generated code exists in `build/generated/kodama/` but the Kotlin compiler cannot resolve references to it when compiling main sources.

### What We Expected
After defining table schemas, we expected to be able to use generated insert methods in main sources:

```kotlin
// Define table with date/timestamp columns
object SimulationRun : Table("simulation_run") {
    val id = integer("id")
    val strategyName = varchar("strategy_name", 255)
    val startDate = date("start_date")              // ✅ DATE type works
    val endDate = date("end_date")                  // ✅ DATE type works
    val executedAt = timestamp("executed_at")        // ✅ TIMESTAMP type works
    // ... other columns
}

// Use generated insert method (expected to work)
val result = SimulationRun.insert(
    transaction = transaction,
    id = 0,
    strategyName = "Buy and Hold",
    startDate = LocalDate.now(),
    endDate = LocalDate.now(),
    executedAt = LocalDateTime.now(),
    // ... other fields
)
```

### What Actually Happened
Compilation errors when trying to use generated methods in main sources:

```
e: Unresolved reference 'generated'.
e: Unresolved reference 'insert'.

FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compileKotlin'.
```

**However, the code generation succeeds:**
```
> Task :generateKodamaExtensions
Kodama: Generated 4 tables, 2 query combinations, 6 column patterns, 0 selection patterns, 0 entity bindings
```

### Investigation Details

The insert methods ARE generated and exist in the correct location:

```bash
$ find build/generated/kodama -name "*.kt" -type f
build/generated/kodama/com/obabichev/trading/schema/generated/QueryExtensions.kt

$ grep -n "fun.*insert" build/generated/kodama/com/obabichev/trading/schema/generated/QueryExtensions.kt
1561:fun com.obabichev.trading.schema.SimulationRun.insert(
1606:fun com.obabichev.trading.schema.SimulationTrade.insert(
```

The generated methods have correct signatures with proper type mappings:

```kotlin
fun com.obabichev.trading.schema.SimulationRun.insert(
    transaction: com.obabichev.kodama.execute.JdbcTransaction,
    id: Int,
    strategyName: String,
    symbol: String,
    startDate: java.time.LocalDate,        // ✅ Correct type mapping
    endDate: java.time.LocalDate,          // ✅ Correct type mapping
    executedAt: java.time.LocalDateTime,   // ✅ Correct type mapping
    // ... other parameters
): com.obabichev.kodama.insert.InsertResult
```

**Root Cause:** The generated code directory (`build/generated/kodama/`) is not being added to the Kotlin compiler's source path before the `:compileKotlin` task runs.

### Current Workaround
Using raw SQL with JDBC prepared statements:

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
stmt.setTimestamp(15, java.sql.Timestamp.valueOf(LocalDateTime.now()))
// ... set all parameters
val rs = stmt.executeQuery()
```

### Impact
- **CRITICAL**: Loss of type safety for insert operations
- Cannot use Kodama's DSL benefits for DML operations
- More verbose code
- Manual parameter binding prone to errors
- Defeats the primary purpose of using Kodama

### Files Affected
- `src/main/kotlin/com/obabichev/trading/persistence/SimulationResultPersistence.kt`

### Related Documentation
See `KODAMA_BUG_REPORT.md` for a detailed bug report with minimal reproducible example.

---

## ✅ RESOLVED: Date/Time Column Types

### Status: RESOLVED ✅

### What Works Now
Kodama v0.2.0 **DOES support** PostgreSQL date and time types with proper Java Time API mapping:

| SQL Type | Kodama Function | Kotlin Type | Status |
|----------|----------------|-------------|---------|
| DATE | `date("column")` | `LocalDate` | ✅ Works |
| TIME | `time("column")` | `LocalTime` | ✅ Works |
| TIMESTAMP | `timestamp("column")` | `LocalDateTime` | ✅ Works |
| TIMESTAMPTZ | `timestampWithTimeZone("column")` | `OffsetDateTime` | ✅ Works |
| TIMETZ | `timeWithTimeZone("column")` | `OffsetTime` | ✅ Works |
| INTERVAL | `interval("column")` | `Duration` | ✅ Works |

### Example Usage

**Table Definition:**
```kotlin
object SimulationRun : Table("simulation_run") {
    val id = integer("id")
    val startDate = date("start_date")              // LocalDate
    val endDate = date("end_date")                  // LocalDate
    val executedAt = timestamp("executed_at")        // LocalDateTime
}
```

**PostgreSQL Migration:**
```sql
CREATE TABLE simulation_run (
    id SERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Working with JDBC (when insert methods aren't available):**
```kotlin
// Proper type conversion for DATE and TIMESTAMP
stmt.setDate(3, java.sql.Date.valueOf(result.startDate))
stmt.setTimestamp(15, java.sql.Timestamp.valueOf(LocalDateTime.now()))
```

### Verification
Tested with actual simulation data - dates are stored and retrieved correctly:

```sql
SELECT id, start_date, end_date, executed_at FROM simulation_run;
 id | start_date |  end_date  |       executed_at
----+------------+------------+-------------------------
  2 | 2023-12-21 | 2025-12-19 | 2025-12-21 18:55:23.456
```

---

## Summary

### What Works ✅
- Date/time column types (`date()`, `timestamp()`, etc.)
- Type-safe table definitions
- Transaction management via `JdbcTransaction`
- Code generation of extension methods
- Query building (in test sources)

### What Doesn't Work ❌
- **Using generated insert methods in main source set** (critical issue)
- Generated extension functions not available during main source compilation

### Recommendation
Until the source set issue is resolved, projects must use raw SQL for INSERT operations in main sources while benefiting from Kodama's type-safe table definitions and query building where available.

---

## Suggestions for Kodama Maintainers

1. **Fix Source Set Registration**: Ensure generated code directory is added to Kotlin source sets before compilation
2. **Task Dependencies**: Verify `:compileKotlin` properly depends on `:generateKodamaExtensions`
3. **Documentation**: If this is intentional behavior, document how to properly configure source sets
4. **Consider Alternative API**: Provide non-generated DSL API for INSERT operations as a fallback

The date/time support is excellent - this is just a Gradle plugin configuration issue that prevents using the otherwise well-designed generated API.
