-- Initial schema for trading simulator

-- Trading strategies table
CREATE TABLE trading_strategy (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Market data table (for historical price data)
CREATE TABLE market_data (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    open_price DECIMAL(18, 8) NOT NULL,
    close_price DECIMAL(18, 8) NOT NULL,
    high_price DECIMAL(18, 8) NOT NULL,
    low_price DECIMAL(18, 8) NOT NULL,
    volume DECIMAL(18, 8) NOT NULL,
    UNIQUE(symbol, timestamp)
);

-- Create index for efficient time-series queries
CREATE INDEX idx_market_data_symbol_timestamp ON market_data(symbol, timestamp);
