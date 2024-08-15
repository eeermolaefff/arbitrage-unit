package com.stambul.arbitrageur.arbitrage.graph.edges.interfaces;

import com.stambul.arbitrageur.arbitrage.graph.edges.SwapEdge;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.MarketDTO;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public abstract class BaseEdge implements DirectedEdge {
    protected CurrencyDTO currencyFrom, currencyTo;
    protected MarketDTO market;
    protected int vertexFrom, vertexTo;
    protected double weight = 0;
    protected double staticCommission = 0, percentCommission = 1;
    protected double minAmount = 0, maxAmount = Double.POSITIVE_INFINITY;

    public BaseEdge(
            CurrencyDTO currencyFrom,
            CurrencyDTO currencyTo,
            MarketDTO market,
            int vertexFrom,
            int vertexTo
    ) {
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.market = market;
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;
    }

    @Override
    public int compareTo(DirectedEdge other) {
        return Comparator.comparing(DirectedEdge::getCurrencyFrom)
                .thenComparing(DirectedEdge::getCurrencyTo)
                .thenComparing(DirectedEdge::getMarket)
                .thenComparingDouble(DirectedEdge::getPercentCommission)
                .thenComparingDouble(DirectedEdge::getStaticCommission)
                .compare(this, other);
    }

    @Override
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new TreeMap<>();

        json.put("market", market.getSlug());
        json.put("ticker", getTicker());
        json.put("weight", weight);
        json.put("staticCommission", staticCommission);
        json.put("percentCommission", percentCommission);
        json.put("minAmount", minAmount);
        json.put("maxAmount", maxAmount);

        Map<String, Object> from = new TreeMap<>();
        from.put("currency", currencyFrom.getSlug());
        json.put("from", from);

        Map<String, Object> to = new TreeMap<>();
        to.put("currency", currencyTo.getSlug());
        json.put("to", to);

        return json;
    };

    @Override
    public double goThrough(double startCurrencyAmount) {
        double newDist = startCurrencyAmount * weight;
        newDist *= percentCommission;
        newDist -= staticCommission;
        return newDist;
    };

    @Override
    public boolean withinTransactionLimits(double startCurrencyAmount) {
        return (startCurrencyAmount <= maxAmount) && (startCurrencyAmount >= minAmount);
    };

    @Override
    public double getStaticCommission() {
        return staticCommission;
    };

    @Override
    public double getPercentCommission() {
        return percentCommission;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public double getMinAmount() {
        return minAmount;
    }

    @Override
    public double getMaxAmount() {
        return maxAmount;
    }

    @Override
    public int getVertexTo() {
        return vertexTo;
    }

    @Override
    public int getVertexFrom() {
        return vertexFrom;
    }

    @Override
    public MarketDTO getMarket() {
        return market;
    }

    @Override
    public CurrencyDTO getCurrencyFrom() {
        return currencyFrom;
    }

    @Override
    public CurrencyDTO getCurrencyTo() {
        return currencyTo;
    }
}
