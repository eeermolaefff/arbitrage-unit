CREATE TABLE IF NOT EXISTS markets (
    id SERIAL PRIMARY KEY,
    slug varchar(50) NOT NULL UNIQUE,
    full_name varchar(50) NOT NULL,
    daily_volume_usd float,
    score float,
    traffic_score float,
    liquidity_score int,
    number_of_markets int,
    number_of_coins int,
    spot_percent_commission float DEFAULT 0,
    date_launched TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tickers (
    id SERIAL PRIMARY KEY,
    ticker varchar(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS currencies (
    id SERIAL PRIMARY KEY,
    slug varchar(60) UNIQUE,
    full_name varchar(60) NOT NULL,
    category varchar(10),
    cex_volume_usd float,
    dex_volume_usd float,
    market_cap_usd float,
    is_active boolean NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS blockchains (
    id SERIAL PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE,
    base_coin_id int,
    gas float,

    FOREIGN KEY(base_coin_id)
        REFERENCES currencies(id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contracts (
    id SERIAL PRIMARY KEY,
    blockchain_id int NOT NULL,
    currency_id int NOT NULL,
    address varchar(120) NOT NULL,

    FOREIGN KEY(currency_id) 
        REFERENCES currencies(id)
            ON DELETE CASCADE,
    FOREIGN KEY(blockchain_id)
        REFERENCES blockchains(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS contracts_currency_id_idx ON contracts(currency_id);
CREATE INDEX IF NOT EXISTS contracts_blockchain_idx ON contracts(blockchain_id);
CREATE INDEX IF NOT EXISTS contracts_cumulative_idx ON contracts(address, blockchain_id, currency_id);

CREATE TABLE IF NOT EXISTS market_currency_relations (
    id SERIAL PRIMARY KEY,
    market_id int NOT NULL,
    currency_id int NOT NULL,
    currency_ticker_id int NOT NULL,
    exchange_type varchar(15) NOT NULL,
    exchange_category varchar(15) NOT NULL,

    FOREIGN KEY(currency_ticker_id)
        REFERENCES tickers(id)
            ON DELETE CASCADE,
    FOREIGN KEY(market_id)
        REFERENCES markets(id)
            ON DELETE CASCADE,
    FOREIGN KEY(currency_id)
        REFERENCES currencies(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS market_currency_relations_currency_id_idx ON market_currency_relations(currency_id);
CREATE INDEX IF NOT EXISTS market_currency_relations_market_id_idx ON market_currency_relations(market_id);
CREATE INDEX IF NOT EXISTS market_currency_relations_cumulative_idx ON
market_currency_relations(market_id, currency_id, exchange_type, exchange_category);

CREATE TABLE IF NOT EXISTS relation_timestamps (
    id SERIAL PRIMARY KEY,
    parent_id int UNIQUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(parent_id)
        REFERENCES currencies(id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contract_timestamps (
    id SERIAL PRIMARY KEY,
    parent_id int UNIQUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(parent_id)
        REFERENCES currencies(id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transfers (
    id SERIAL PRIMARY KEY,
    market_currency_relation_id int NOT NULL,
    blockchain_id int NOT NULL,
    withdraw_enable boolean,
    deposit_enable boolean,
    withdrawal_static_commission float,
    deposit_static_commission float,
    withdrawal_percent_commission float,
    deposit_percent_commission float,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(market_currency_relation_id)
        REFERENCES market_currency_relations(id)
            ON DELETE CASCADE,
    FOREIGN KEY(blockchain_id)
        REFERENCES blockchains(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS transfers_cumulative_idx ON transfers(market_currency_relation_id, blockchain_id);

CREATE TABLE IF NOT EXISTS trading_pairs (
    id SERIAL PRIMARY KEY,
    market_id int NOT NULL,
    base_asset_id int NOT NULL,
    quote_asset_id int NOT NULL,
    ticker_id int NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(ticker_id)
        REFERENCES tickers(id)
            ON DELETE CASCADE,
    FOREIGN KEY(market_id) 
        REFERENCES markets(id)
            ON DELETE CASCADE,
    FOREIGN KEY(base_asset_id) 
        REFERENCES currencies(id)
            ON DELETE CASCADE,
    FOREIGN KEY(quote_asset_id) 
        REFERENCES currencies(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS trading_pairs_cumulative_idx ON trading_pairs(market_id, base_asset_id, quote_asset_id);
CREATE INDEX IF NOT EXISTS trading_pairs_market_idx ON trading_pairs(market_id);

CREATE TABLE IF NOT EXISTS orderbooks (
    id SERIAL PRIMARY KEY,
    trading_pair_id int NOT NULL,
    is_active boolean,
    bid_price float,
    ask_price float,
    bid_qty float,
    ask_qty float,
    daily_volume_usd float,
    daily_volume_base float,
    daily_volume_quote float,
    is_spot_trading_allowed boolean,
    is_margin_trading_allowed boolean,
    trading_type varchar(15),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(trading_pair_id)
        REFERENCES trading_pairs(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS orderbooks_trading_pair_idx ON orderbooks(trading_pair_id);

CREATE TABLE IF NOT EXISTS swaps (
    id SERIAL PRIMARY KEY,
    trading_pair_id int NOT NULL,
    base_contract_id int NOT NULL,
    quote_contract_id int NOT NULL,
    is_active boolean,
    hash varchar(42),
    fee_percentage float,
    liquidity float,
    base_price float,
    quote_price float,
    daily_volume_usd float,
    daily_volume_base float,
    daily_volume_quote float,
    tvl_usd float,
    tvl_base float,
    tvl_quote float,
    trading_type varchar(15),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    FOREIGN KEY(trading_pair_id)
        REFERENCES trading_pairs(id)
            ON DELETE CASCADE,
    FOREIGN KEY(base_contract_id)
        REFERENCES contracts(id)
            ON DELETE CASCADE,
    FOREIGN KEY(quote_contract_id)
        REFERENCES contracts(id)
            ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS swaps_trading_pair_idx ON swaps(trading_pair_id);
CREATE INDEX IF NOT EXISTS swaps_cumulative_idx ON swaps(trading_pair_id, base_contract_id, quote_contract_id, fee_percentage);

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW(); 
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp_trading_pairs
BEFORE UPDATE ON trading_pairs
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_orderbooks
BEFORE UPDATE ON orderbooks
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_swaps
BEFORE UPDATE ON swaps
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_currencies
BEFORE UPDATE ON currencies
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_markets
BEFORE UPDATE ON markets
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_relations
BEFORE UPDATE ON relation_timestamps
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_contracts
BEFORE UPDATE ON contract_timestamps
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_transfers
BEFORE UPDATE ON transfers
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

