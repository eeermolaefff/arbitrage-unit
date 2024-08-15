package com.stambul.arbitrageur.arbitrage.cycles.objects;

import com.stambul.arbitrageur.arbitrage.cycles.objects.interfaces.Cycle;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.*;

public class ProfitComparableCycle extends Cycle<ProfitComparableCycle> {
    protected final double startCurrencyAmount;
    protected double profitAsPercent;

    public ProfitComparableCycle(List<DirectedEdge> cycle, double startCurrencyAmount) {
        super(cycle);
        this.startCurrencyAmount = startCurrencyAmount;
        profitAsPercent = calculateProfitAsPercent();
    }

    public double updateProfit() {
        stringRepresentation = null;
        profitAsPercent = calculateProfitAsPercent();
        return profitAsPercent;
    }

    public double getStartCurrencyAmount() {
        return startCurrencyAmount;
    }

    public double getProfitAsPercent() {
        return profitAsPercent;
    }

    protected double calculateProfitAsPercent() {
        double currentCurrencyAmount = startCurrencyAmount;
        for (DirectedEdge edge : cycle)
            currentCurrencyAmount = edge.goThrough(currentCurrencyAmount);
        return  (currentCurrencyAmount - startCurrencyAmount) * 100 / startCurrencyAmount;
    }

    @Override
    protected Map<String, Object> buildCycleAsJson() {
        Map<String, Object> cycleAsJson = super.buildCycleAsJson();
        cycleAsJson.put("profitAsPercent", profitAsPercent);
        return cycleAsJson;
    }

    @Override
    protected List<Object> buildEdgesAsJson(Iterable<DirectedEdge> edges) {
        return buildEdgesAsJson(edges, startCurrencyAmount);
    }

    @Override
    public int compareTo(ProfitComparableCycle another) {
        return Comparator.comparingInt(ProfitComparableCycle::getCycleSize).reversed()
                .thenComparingDouble(ProfitComparableCycle::getProfitAsPercent)
                .compare(this, another);
    }
}
