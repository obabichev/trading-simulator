# Fetching Historical Stock Market Data
*(Sources, APIs, Data Structures — No Code)*

This document explains **how to fetch historical stock market data** for backtesting trading strategies.  
It focuses on **data sources**, **API usage patterns**, **returned data structures**, and **normalization concerns**, without including any implementation code.

The goal is to help another LLM (or developer) reliably implement a data ingestion pipeline.

---

## 1) What “Historical Stock Data” Usually Includes

For equities, historical data almost always means **candlestick (OHLCV) bars**:

- **Open** – price at the start of the session/interval
- **High** – highest traded price
- **Low** – lowest traded price
- **Close** – price at session close
- **Volume** – number of shares traded

### Timeframes
- **Daily (`1d`)** → most common and reliable
- **Intraday (`1h`, `30m`, `5m`, `1m`)** → often limited or paid

### Corporate Actions (Critical for Stocks)
Stock prices are affected by:
- stock splits
- reverse splits
- dividends

Because of this, most providers distinguish between:
- **Unadjusted prices** (raw market prices)
- **Adjusted prices** (historically corrected for splits/dividends)

➡️ For most backtests, **adjusted daily prices are strongly recommended**.

---

## 2) Categories of Stock Data Sources

### A) Free Public Daily Data (Best for Learning & Prototyping)

#### **:contentReference[oaicite:0]{index=0}**
- Free daily historical prices (CSV)
- Covers US and international stocks, ETFs, and indices

**Strengths**
- No API key
- Easy bulk access
- Long history for many symbols

**Limitations**
- Primarily daily data
- Corporate actions handling is limited compared to paid vendors
- No guarantees on data completeness

**Best use**
- Strategy prototyping
- Long-term daily strategies
- Academic or personal research

---

### B) Retail Aggregator APIs (Best Balance of Simplicity & Features)

#### **:contentReference[oaicite:1]{index=1}**
- Widely used API for stocks, ETFs, FX
- Supports **adjusted daily equity data**

**Strengths**
- Clear documentation
- Adjusted and unadjusted endpoints
- JSON or CSV output
- Includes metadata (timezone, currency, symbol)

**Limitations**
- Strict rate limits on free tier
- Intraday history is limited unless paid

**Best use**
- Daily equity backtesting
- Reproducible pipelines
- When corporate actions matter

---

### C) Institutional / Paid Providers (Mentioned for Completeness)

Examples:
- exchange data products
- Nasdaq Data Link
- Polygon, Tiingo, Quandl datasets

**Strengths**
- High data quality
- Accurate corporate actions
- Long intraday history

**Limitations**
- Paid
- Licensing constraints

**Best use**
- Professional or production-grade research

---

## 3) How Stock Market Data APIs Typically Work

Regardless of provider, equity APIs follow similar patterns.

### Common Request Parameters
- **Symbol**: e.g. `AAPL`, `MSFT`
- **Timeframe / Interval**: `1d`, `1h`, etc.
- **Date range**:
    - explicit start/end dates
    - or provider-defined “full” vs “compact” modes
- **Adjusted flag**: whether prices include splits/dividends
- **Output format**: JSON or CSV

### Rate Limits
- Free tiers usually limit:
    - requests per minute
    - requests per day
- Fetching many symbols requires batching and caching

---

## 4) Typical Data Structures Returned

### A) Daily Stock Data (JSON-style)

Most APIs return:
- **Metadata section**
    - symbol
    - exchange
    - timezone
    - last refreshed date
    - currency

- **Time series section**
    - keyed by date (e.g. `"2024-03-15"`)
    - values include:
        - open
        - high
        - low
        - close
        - volume
        - adjusted close (if applicable)
        - dividend amount
        - split coefficient

Dates are usually:
- strings (`YYYY-MM-DD`)
- implicitly in the exchange’s trading calendar

---

### B) Daily Stock Data (CSV-style)

CSV files usually contain:
- Date
- Open
- High
- Low
- Close
- Volume
- Sometimes:
    - Adjusted Close
    - Dividend
    - Split ratio

CSV files often **omit metadata**, so you must store it yourself.

---

## 5) Adjusted vs. Unadjusted Prices (Very Important)

### Unadjusted Prices
- Reflect actual traded prices on that day
- Historical prices become misleading after splits

### Adjusted Prices
- Past prices are recalculated so the series is continuous
- Essential for:
    - moving averages
    - returns
    - drawdown calculations
    - ML models

**Rule of thumb**
> If your strategy spans long time periods, always use adjusted prices.

Never mix:
- adjusted and unadjusted prices
- prices from different adjustment methodologies

---

## 6) Normalizing Stock Data for Backtesting

Even if providers differ, your system should use **one canonical schema**.

### Recommended Normalized Schema (Daily Stocks)

| Field | Description |
|-----|-------------|
| symbol | Stock ticker |
| exchange | Listing exchange (if known) |
| timeframe | `1d` |
| timestamp | Candle open time in UTC (epoch) |
| open | Float |
| high | Float |
| low | Float |
| close | Float |
| volume | Shares traded |
| adjusted_close | Float (optional but recommended) |
| dividend | Float (optional) |
| split_factor | Float (optional) |
| is_adjusted | Boolean |
| source | Provider name |
| fetched_at | UTC timestamp when data was fetched |

### Normalization Rules
- Convert dates to **UTC timestamps**
- Sort by timestamp ascending
- Ensure numeric values are numeric (not strings)
- Deduplicate `(symbol, timeframe, timestamp)`

---

## 7) Data Quality Checks (Stocks)

Always validate:

1. **Chronological order**
    - No backward time jumps

2. **OHLC sanity**
    - high ≥ open, close
    - low ≤ open, close
    - high ≥ low

3. **Trading calendar**
    - Missing weekends/holidays are expected
    - Missing weekdays may indicate data gaps

4. **Corporate actions**
    - Large overnight price jumps often indicate splits
    - Confirm adjustment consistency

---

## 8) Recommended Fetching Strategy (Conceptual)

### Initial Load
1. Fetch **full historical daily data** per symbol
2. Prefer adjusted data
3. Store locally as immutable historical records
4. Save metadata:
    - provider
    - adjustment type
    - retrieval date
    - parameters used

### Incremental Updates
- On subsequent runs:
    - fetch only data newer than the last stored trading day
    - append and revalidate

### Reproducibility
- Always record:
    - provider name
    - endpoint type
    - whether data was adjusted
    - fetch timestamp

This allows you to reproduce old backtests exactly.

---

## 9) Common Pitfalls in Stock Data

- Mixing adjusted and unadjusted prices
- Ignoring dividends in long-term strategies
- Assuming all providers use the same adjustment logic
- Not storing metadata
- Treating daily data as timezone-agnostic
- Backtesting intraday logic on daily bars

---

## 10) Default Recommendation (Stocks Only)

If you want a **safe and simple default**:

- **Daily equities**
    - Use an API that provides **adjusted daily prices**
    - Normalize to a single OHLCV schema
    - Store locally and version your dataset

This setup is sufficient for:
- trend-following
- mean reversion
- factor-based strategies
- portfolio simulations

---

## Appendix: Implementation Checklist (for an LLM)

When implementing a stock data connector, ensure:

- [ ] symbol format mapping
- [ ] adjusted vs unadjusted selection
- [ ] correct trading calendar handling
- [ ] rate-limit awareness
- [ ] date → UTC timestamp normalization
- [ ] schema normalization
- [ ] deduplication
- [ ] metadata persistence
- [ ] incremental updates

---
