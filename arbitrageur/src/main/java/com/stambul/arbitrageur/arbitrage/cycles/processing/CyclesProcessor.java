package com.stambul.arbitrageur.arbitrage.cycles.processing;

import com.stambul.arbitrageur.arbitrage.cycles.objects.ConnectedCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.*;

public class CyclesProcessor {
    private final Map<Integer, List<Arbitrage>> arbitrageMap = new TreeMap<>();
    private final Map<Integer, Set<FieldsComparableCycle>> foundCyclesMap = new TreeMap<>();
    private final double minProfitAsPercent, maxProfitAsPercent;

    public CyclesProcessor(double minProfitAsPercent, double maxProfitAsPercent) {
        this.minProfitAsPercent = minProfitAsPercent;
        this.maxProfitAsPercent = maxProfitAsPercent;
    }

    public void addAll(int componentId, Arbitrage arbitrage, Set<FieldsComparableCycle> foundCycles) {
        arbitrageMap.computeIfAbsent(componentId, x -> new LinkedList<>()).add(arbitrage);
        foundCyclesMap.computeIfAbsent(componentId, x -> new TreeSet<>()).addAll(foundCycles);
    }

    public void addFoundCycles(int componentId, Arbitrage arbitrage, Set<FieldsComparableCycle> foundCycles) {
        foundCyclesMap.computeIfAbsent(componentId, x -> new TreeSet<>()).addAll(foundCycles);
    }

    public List<Arbitrage> getArbitrages(int componentId) {
        return arbitrageMap.get(componentId);
    }

    public Map<FieldsComparableCycle, ProfitComparableCycle> getAllCycles() {
        Map<FieldsComparableCycle, ProfitComparableCycle> cyclesMap = new TreeMap<>();

        for (int component : arbitrageMap.keySet()) {
            List<Arbitrage> arbitrages = arbitrageMap.get(component);
            Set<FieldsComparableCycle> foundCycles = foundCyclesMap.get(component);

            for (FieldsComparableCycle foundCycle : foundCycles) {
                ProfitComparableCycle cycle = makeMeasurable(foundCycle, arbitrages);
                double profit = cycle.getProfitAsPercent();
                if (profit >= minProfitAsPercent && profit <= maxProfitAsPercent)
                    cyclesMap.put(foundCycle, cycle);
            }
        }

        return cyclesMap;
    }

    private ProfitComparableCycle makeMeasurable(
            FieldsComparableCycle fieldsComparableCycle,
            List<Arbitrage> arbitrages
    ) {
        ProfitComparableCycle result = shift(fieldsComparableCycle, arbitrages);
        if (result != null)
            return result;
        return connect(fieldsComparableCycle, arbitrages);
    }

    private ProfitComparableCycle shift(
            FieldsComparableCycle fieldsComparableCycle,
            List<Arbitrage> arbitrages
    ) {
        for (Arbitrage arbitrage : arbitrages) {
            int position = fieldsComparableCycle.positionOf(arbitrage.getStartVertex());
            if (position != -1) {
                List<DirectedEdge> cycle = fieldsComparableCycle.getCycle(position);
                return new ProfitComparableCycle(cycle, arbitrage.getStartStake());
            }
        }
        return null;
    }

    private ConnectedCycle connect(
            FieldsComparableCycle fieldsComparableCycle,
            List<Arbitrage> arbitrages
    ) {
        List<DirectedEdge> cycle = fieldsComparableCycle.getCycle();
        int startVertex = cycle.get(0).getVertexFrom();
        double startCurrencyAmount = arbitrages.get(0).getDistanceTo(startVertex);

        ConnectedCycle connectedCycle = new ConnectedCycle(cycle, startCurrencyAmount);
        for (Arbitrage arbitrage : arbitrages)
            connectedCycle.connect(
                    arbitrage.getStartVertex(),
                    arbitrage.getStartStake(),
                    arbitrage.getPathTo(startVertex)
            );
        return connectedCycle;
    }
}
