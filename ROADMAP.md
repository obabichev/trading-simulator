# Trading Simulator Roadmap

## Project Goals

### Primary Goal
Build a trading strategy simulator that:
- Simulates trading strategies on historical market data
- Collects and analyzes strategy performance results
- Provides insights into strategy effectiveness

### Secondary Goal
Demonstrate practical usage of Kodama - a Kotlin PostgreSQL database library

## Development Phases

### Phase 1: Database Infrastructure Setup ✅ COMPLETED
**Objective**: Establish database configuration and tooling

- [x] Set up PostgreSQL (Docker Compose)
- [x] Configure PostgreSQL connection (port 5455)
- [x] Integrate Kodama library (v0.3.0 with serial columns)
- [x] Configure Flyway migrations
- [x] Create initial schema (trading_strategy, market_data tables)
- [x] Verify database connectivity and basic operations
- [x] Write comprehensive Kodama integration tests (7/7 passing)

**Status**: ✅ Complete - Upgraded to Kodama 0.3.0 with full auto-increment support

### Phase 2: Historical Data Acquisition ✅ COMPLETED
**Objective**: Source and import historical market data

- [x] Identify historical data sources (Yahoo Finance)
- [x] Implement sample data generator (Gaussian random walk)
- [x] Generate realistic sample data (8 symbols × 522 days)
- [x] Store data in CSV files (tdata/ directory)
- [x] Implement CSV parser (MarketDataCsvParser)
- [x] Validate data integrity

**Status**: ✅ Complete - Sample data generated with realistic price movements

### Phase 3: Data Persistence Layer ✅ COMPLETED
**Objective**: Store historical data using Kodama

- [x] Design database schema for market data
- [x] Design schema for simulation results (simulation_run, simulation_trade)
- [x] Use Kodama with proper date/timestamp types
- [x] Use serial columns for auto-increment primary keys
- [x] Implement data storage with type-safe inserts
- [x] Create data access layer (SimulationResultPersistence)

**Status**: ✅ Complete - All persistence using Kodama's type-safe DSL

### Phase 4: Strategy Simulation Engine ✅ COMPLETED
**Objective**: Build the core simulation framework

- [x] Define strategy interface (TradingStrategy)
- [x] Implement simulation execution engine (SimulationEngine)
- [x] Process historical data through strategies
- [x] Track trades and positions (Portfolio model)
- [x] Implement immutable portfolio management
- [x] Create first strategy (BuyAndHoldStrategy)

**Status**: ✅ Complete - Simulation engine runs successfully end-to-end

### Phase 5: Results Collection & Analysis ✅ PARTIALLY COMPLETE
**Objective**: Capture and analyze simulation results

- [x] Store simulation results in database
- [x] Store individual trades
- [x] Calculate performance metrics:
  - [x] Total return (percentage and amount)
  - [x] Max drawdown
  - [x] Sharpe ratio
  - [x] Win/loss ratio
  - [x] Total trades count
- [x] Track execution time
- [ ] ⏳ Query and compare past simulations
- [ ] ⏳ Generate reports and analytics
- [ ] ⏳ Visualize strategy performance
- [ ] ⏳ Export results to CSV/JSON

**Status**: 🔄 In Progress - Core metrics calculated, visualization pending

---

## Current Status

**Overall Progress**: ~85% Complete

**Recently Completed**:
- ✅ Upgraded to Kodama 0.3.0
- ✅ Integrated serial columns for auto-increment IDs
- ✅ Full end-to-end simulation working
- ✅ Database persistence with proper type mapping

**Next Focus**: Phase 6 - Enhancement & Polish

---

## Phase 6: Enhancement & Polish (NEXT)

### 6.1 Fix Test Suite ✅ COMPLETED
**Priority**: High

- [x] Update tests to use Kodama 0.3.0 API (`from()` instead of `query().from()`)
- [x] Remove `id` parameters from test insert calls
- [x] Update test assertions for new generated methods
- [x] Verify all 7 tests pass

**Status**: ✅ Complete - All 7 integration tests passing with Kodama 0.3.0

### 6.2 Additional Trading Strategies 📈
**Priority**: High (demonstrates core functionality)

Implement additional strategies to test simulation framework:

- [ ] **Moving Average Crossover**
  - Buy when short MA crosses above long MA
  - Sell when short MA crosses below long MA
  - Parameters: short period (e.g., 20), long period (e.g., 50)

- [ ] **RSI Strategy**
  - Buy when RSI < 30 (oversold)
  - Sell when RSI > 70 (overbought)
  - Parameter: RSI period (e.g., 14)

- [ ] **Momentum Strategy**
  - Buy when price momentum is positive
  - Sell when momentum turns negative
  - Parameter: lookback period

- [ ] **Mean Reversion**
  - Buy when price deviates below mean
  - Sell when price deviates above mean
  - Parameters: period, standard deviation threshold

**Effort**: 4-6 hours (2-3 strategies)

### 6.3 Query and Compare Results 🔍
**Priority**: Medium

Use Kodama's query DSL to analyze past simulations:

- [ ] Query all simulation runs
- [ ] Filter by strategy, symbol, date range
- [ ] Sort by performance metrics
- [ ] Compare strategies side-by-side
- [ ] Find best performing strategy for each symbol

**Example**:
```kotlin
val topStrategies = from(SimulationRun)
    .selectAll(SimulationRun)
    .where { simulationRun.totalReturn gt BigDecimal.ZERO }
    .orderBy { -simulationRun.totalReturn.desc() }
    .limit(10)
    .execute(transaction)
```

**Effort**: 2-3 hours

### 6.4 Real Market Data 📊
**Priority**: Medium

Replace sample data with real historical data:

- [ ] Implement Yahoo Finance downloader (authentication handling)
- [ ] Download real data for major symbols (AAPL, GOOGL, MSFT, etc.)
- [ ] Support date range selection
- [ ] Handle weekends/holidays (market closed days)
- [ ] Optional: Cache downloaded data to avoid re-fetching

**Alternative**: Provide instructions for manual download from Yahoo Finance

**Effort**: 3-4 hours

### 6.5 CLI Interface 💻
**Priority**: Low (nice to have)

Add interactive command-line interface:

- [ ] Choose strategy from available strategies
- [ ] Select symbol(s) to test
- [ ] Specify date range
- [ ] Set initial budget
- [ ] Display results in formatted table
- [ ] Option to save to database or just display

**Example**:
```
$ ./gradlew run --args="--strategy=MovingAverageCrossover --symbol=AAPL --from=2023-01-01 --to=2024-01-01 --budget=10000"
```

**Effort**: 3-4 hours

### 6.6 Batch Testing 🔄
**Priority**: Low

Test multiple strategies across multiple symbols:

- [ ] Run all strategies on all symbols
- [ ] Compare performance across different market conditions
- [ ] Generate comparison report
- [ ] Identify most robust strategies

**Effort**: 2-3 hours

### 6.7 Performance Optimization 🚀
**Priority**: Low

Optimize simulation execution:

- [ ] Profile simulation performance
- [ ] Optimize data loading (lazy loading, caching)
- [ ] Parallel simulation execution
- [ ] Batch database inserts for trades

**Effort**: 2-4 hours

### 6.8 Visualization & Reporting 📈
**Priority**: Low (requires external library)

Generate visual reports:

- [ ] Export results to CSV
- [ ] Generate JSON reports
- [ ] Create equity curve charts (requires charting library)
- [ ] Trade visualization
- [ ] Strategy comparison charts

**Options**:
- Export to CSV and use external tools (Excel, Python)
- Integrate Kotlin charting library (e.g., kravis, lets-plot)
- Generate HTML reports with charts

**Effort**: 4-8 hours (depends on approach)

---

## Recommended Next Steps (Priority Order)

1. **Fix Tests** (1-2 hours) - Get clean build
2. **Add 2-3 More Strategies** (4-6 hours) - Demonstrate simulation variety
3. **Query Results** (2-3 hours) - Show Kodama query features
4. **Real Data** (3-4 hours) - More realistic results
5. **CLI Interface** (3-4 hours) - Better user experience
6. **Batch Testing** (2-3 hours) - Strategy comparison
7. **Visualization** (4-8 hours) - Polish

**Quick Win Path** (1-2 days):
1. Fix tests
2. Add MovingAverageCrossover strategy
3. Add query functionality to compare strategies
4. Document results

**Full Featured Path** (1 week):
All of the above + real data + CLI + visualization

---

## Success Criteria

The project will be considered complete when:

- ✅ Multiple trading strategies implemented (3-5 strategies)
- ✅ Strategies can be backtested on historical data
- ✅ Results stored in database with comprehensive metrics
- ✅ Past simulations can be queried and compared
- ✅ All tests passing
- ✅ Documentation complete
- ⏳ Real market data integration (optional)
- ⏳ Visualization/reporting (optional)

**Current**: 5/8 criteria met (62%)

---

## Technical Debt

None identified - codebase is clean and well-structured:
- ✅ Proper separation of concerns
- ✅ Type-safe database operations
- ✅ Immutable data models
- ✅ Clean architecture
- ✅ Good documentation

---

## Future Enhancements (Beyond Current Scope)

- Portfolio management (multiple positions)
- Position sizing strategies
- Risk management (stop-loss, take-profit)
- Transaction costs and slippage
- Real-time trading simulation
- Machine learning strategy optimization
- Walk-forward analysis
- Monte Carlo simulation
- Web interface

---

## Questions for Discussion

1. **Which strategies would you like to implement next?**
   - Moving Average Crossover (simple, commonly used)
   - RSI (popular momentum indicator)
   - Custom strategy?

2. **Real data priority?**
   - Use sample data for now (works fine for testing)
   - Implement real Yahoo Finance downloader
   - Manual download instructions

3. **Visualization approach?**
   - Export to CSV and use external tools
   - Integrate Kotlin charting library
   - Generate HTML reports
   - Skip visualization for now

4. **Focus on breadth or depth?**
   - Many simple strategies (breadth)
   - Fewer complex strategies with more features (depth)

Let me know which direction interests you most!
