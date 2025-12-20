-- Table to store trading strategy simulation runs and their results

CREATE TABLE simulation_run (
    id SERIAL PRIMARY KEY,
    strategy_name VARCHAR(255) NOT NULL,
    symbol VARCHAR(50) NOT NULL,

    -- Date range of the simulation
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Budget and performance
    initial_budget DECIMAL(18, 2) NOT NULL,
    final_budget DECIMAL(18, 2) NOT NULL,
    total_return DECIMAL(18, 4) NOT NULL,          -- Percentage return
    total_return_amount DECIMAL(18, 2) NOT NULL,   -- Dollar amount

    -- Trading statistics
    total_trades INTEGER NOT NULL DEFAULT 0,
    winning_trades INTEGER NOT NULL DEFAULT 0,
    losing_trades INTEGER NOT NULL DEFAULT 0,

    -- Performance metrics
    max_drawdown DECIMAL(18, 4),                   -- Maximum percentage loss from peak
    sharpe_ratio DECIMAL(18, 4),                   -- Risk-adjusted return metric

    -- Metadata
    data_points_used INTEGER NOT NULL,             -- Number of days of market data used
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms BIGINT,                      -- How long the simulation took

    -- Additional notes
    notes TEXT
);

CREATE INDEX idx_simulation_run_strategy ON simulation_run(strategy_name);
CREATE INDEX idx_simulation_run_symbol ON simulation_run(symbol);
CREATE INDEX idx_simulation_run_executed_at ON simulation_run(executed_at);

-- Optional: Table to store individual trades from simulations
CREATE TABLE simulation_trade (
    id SERIAL PRIMARY KEY,
    simulation_run_id INTEGER NOT NULL REFERENCES simulation_run(id) ON DELETE CASCADE,

    trade_date DATE NOT NULL,
    action VARCHAR(10) NOT NULL,  -- 'BUY' or 'SELL'

    shares DECIMAL(18, 8) NOT NULL,
    price DECIMAL(18, 8) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,

    portfolio_value DECIMAL(18, 2) NOT NULL,  -- Portfolio value after this trade

    notes TEXT
);

CREATE INDEX idx_simulation_trade_run_id ON simulation_trade(simulation_run_id);
CREATE INDEX idx_simulation_trade_date ON simulation_trade(trade_date);
