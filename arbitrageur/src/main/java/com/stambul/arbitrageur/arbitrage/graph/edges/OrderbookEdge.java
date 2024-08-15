package com.stambul.arbitrageur.arbitrage.graph.edges;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.BaseEdge;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.library.database.objects.dto.OrderbookDTO;

import java.util.Map;
import java.util.TreeMap;

public class OrderbookEdge extends BaseEdge {
    private OrderbookDTO orderbook;

    public OrderbookEdge(
            CurrencyDTO currencyFrom,
            CurrencyDTO currencyTo,
            MarketDTO market,
            OrderbookDTO orderbook,
            int vertexFrom,
            int vertexTo
    ) {
        super(currencyFrom, currencyTo, market, vertexFrom, vertexTo);
        init(orderbook);
    }

    public OrderbookDTO getOrderbook() {
        return orderbook;
    }

    public void update(OrderbookDTO orderbook) {
        init(orderbook);
    }

    @Override
    public Map<String, Object> toJSON(double startCurrencyAmount) {
        Map<String, Object> json = super.toJSON();

        json.put("tradingType", orderbook.getTradingType());
        json.put("updatedAt", orderbook.getUpdatedAt());

        Map<String, Object> from = (Map<String, Object>) json.get("from");
        Map<String, Object> to = (Map<String, Object>) json.get("to");

        from.put("amount", startCurrencyAmount);
        to.put("amount", goThrough(startCurrencyAmount));

        return json;
    };

    @Override
    public DirectedEdge copy(int vertexFrom, int vertexTo) {
        return new OrderbookEdge(currencyFrom, currencyTo, market, orderbook, vertexFrom, vertexTo);
    }

    @Override
    public DirectedEdge reversed() {
        return new OrderbookEdge(currencyTo, currencyFrom, market, orderbook, vertexTo, vertexFrom);
    }

    @Override
    public String getTicker() {
        return orderbook.getTradingPair().getTicker().getTicker();
    }

    @Override
    public String toString() {
        return String.format(
                "%s: %s %s -> %s (%.5f) [%f, %f]",
                market.getFullName(),
                getTicker(),
                currencyFrom.getSlug(),
                currencyTo.getSlug(),
                weight,
                minAmount,
                maxAmount
        );
    }

    private void init(OrderbookDTO orderbook) {
        this.orderbook = orderbook;
        percentCommission = (100 - market.getSpotPercentCommission()) / 100;

        if (orderbook.getTradingPair().getBaseAssetId().equals(currencyFrom.getId())) {  // base to quote
            weight = orderbook.getBidPrice();
            maxAmount = orderbook.getBidQty();
        } else if (orderbook.getTradingPair().getQuoteAssetId().equals(currencyFrom.getId())) {  // quote to base
            weight = 1 / orderbook.getAskPrice();
            maxAmount = orderbook.getAskQty() * orderbook.getAskPrice();
        } else {
            String message = "Base/quote ids mismatch: fromIdx=%d, toIdx=%d, edge=%s";
            throw new IllegalArgumentException(String.format(message, currencyFrom.getId(), currencyTo.getId(), this));
        }
    }

}
