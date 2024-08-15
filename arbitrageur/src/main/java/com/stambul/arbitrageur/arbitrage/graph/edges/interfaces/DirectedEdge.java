package com.stambul.arbitrageur.arbitrage.graph.edges.interfaces;

import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.MarketDTO;

import java.util.Map;

public interface DirectedEdge extends Comparable<DirectedEdge> {
    double getStaticCommission();
    double getPercentCommission();
    double getWeight();
    double getMinAmount();
    double getMaxAmount();
    int getVertexTo();
    int getVertexFrom();
    DirectedEdge reversed();
    CurrencyDTO getCurrencyFrom();
    CurrencyDTO getCurrencyTo();
    MarketDTO getMarket();
    DirectedEdge copy(int vertexFrom, int vertexTo);
    double goThrough(double startCurrencyAmount);
    boolean withinTransactionLimits(double startCurrencyAmount);
    String getTicker();
    Map<String, Object> toJSON(double startCurrencyAmount);
    Map<String, Object> toJSON();
}
