TABLE markets {
    id int [PRIMARY KEY]
    slug varchar(50) [UNIQUE]
    full_name varchar(30)
    daily_volume_usd float
    score float
    traffic_score float
    liquidity_score int
    number_of_markets int
    number_of_coins int
    date_launched TIMESTAMPTZ
    updated_at TIMESTAMPTZ [NOT NULL]
}

TABLE tickers {
    id int [PRIMARY KEY]
    ticker varchar(50) [UNIQUE]
}

TABLE currencies {
    id int [PRIMARY KEY]
    slug varchar(60) [UNIQUE]
    full_name varchar(60) [NOT NULL]
    category varchar(10)
    cex_volume_usd float
    dex_volume_usd float
    market_cap_usd float
    is_active boolean [NOT NULL]
    updated_at TIMESTAMPTZ [NOT NULL]
}

TABLE blockchains {
    id int [PRIMARY KEY]
    base_coin_id int [NOT NULL]
    gas float
    name varchar(50) [NOT NULL]
}
Ref: blockchains.base_coin_id > currencies.id

TABLE contracts {
    id int [PRIMARY KEY]
    currency_id int [NOT NULL]
    blockchain_id int [NOT NULL]
    address varchar(30) [NOT NULL]
}
Ref: contracts.blockchain_id > blockchains.id
Ref: contracts.currency_id > currencies.id

Table market_currency_relations {
    id int [PRIMARY KEY]
    market_id int [NOT NULL]
    currency_ticker_id int [NOT NULL]
    currency_id int [NOT NULL]
    exchange_type varchar(15) [NOT NULL]
    exchange_category varchar(15) [NOT NULL]
}
Ref: market_currency_relations.currency_ticker_id > tickers.id
Ref: market_currency_relations.market_id > markets.id
Ref: market_currency_relations.currency_id > currencies.id

Table relation_timestamps {
    id int [PRIMARY KEY]
    currency_id int [UNIQUE]
    updated_at TIMESTAMPTZ [NOT NULL]
}
Ref: relation_timestamps.currency_id > currencies.id

Table contract_timestamps {
    id int [PRIMARY KEY]
    currency_id int [UNIQUE]
    updated_at TIMESTAMPTZ [NOT NULL]
}
Ref: contract_timestamps.currency_id > currencies.id


Table transfers {
  id int [PRIMARY KEY]
  markets_currencies_relation_id int
  blockchain_id int
  withdrawal_enable boolean
  deposit_enable boolean
  withdraw_static_fee float
  withdraw_procent_fee float
  deposit_static_fee float
  deposit_procent_fee float
  updated_at TIMESTAMPTZ
}
Ref: transfers.blockchain_id > blockchains.id
Ref: transfers.markets_currencies_relation_id > market_currency_relations.id


TABLE trading_pairs {
    id int [PRIMARY KEY]
    market_id int
    ticker_id int
    base_asset_id int
    quote_asset_id int
}

Ref: trading_pairs.ticker_id > tickers.id
Ref: trading_pairs.market_id > markets.id
Ref: trading_pairs.base_asset_id > currencies.id
Ref: trading_pairs.quote_asset_id > currencies.id


TABLE trading_pairs_orderbook {
    id int [PRIMARY KEY]
    trading_pair_id int [NOT NULL]
    is_active boolean
    bid_price float
    ask_price float
    bid_qty float
    ask_qty float
    day_volume_usd float
    day_volume_base float
    day_volume_quote float
    is_spot_allowed boolean
    is_margin_allowed boolean
    trading_type varchar(15)
    updated_at TIMESTAMPTZ
}

Ref: trading_pairs_orderbook.trading_pair_id > trading_pairs.id


TABLE trading_pairs_swap {
    id int [PRIMARY KEY]
    trading_pair_id int [NOT NULL]
    is_active boolean
    liquidity float
    base_price float
    quote_price float
    day_volume_usd float
    day_volume_base float
    day_volume_quote float
    tvl_usd float
    tvl_base float
    tvl_quote float
    trading_type varchar(15)
    updated_at TIMESTAMPTZ
}

Ref: trading_pairs_swap.trading_pair_id > trading_pairs.id