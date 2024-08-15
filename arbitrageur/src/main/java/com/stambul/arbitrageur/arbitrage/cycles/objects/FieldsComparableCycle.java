package com.stambul.arbitrageur.arbitrage.cycles.objects;

import com.stambul.arbitrageur.arbitrage.cycles.objects.interfaces.Cycle;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.*;

public class FieldsComparableCycle extends Cycle<FieldsComparableCycle> {
    private final List<DirectedEdge> sortedEdges = new ArrayList<>();

    public FieldsComparableCycle(List<DirectedEdge> cycle) {
        super(cycle);
        sortedEdges.addAll(cycle);
        Collections.sort(sortedEdges);
    }

    @Override
    public int compareTo(FieldsComparableCycle another) {
        if (this.sortedEdges.size() != another.sortedEdges.size())
            return Integer.compare(this.sortedEdges.size(), another.sortedEdges.size());

        for (int i = 0; i < this.sortedEdges.size(); i++) {
            DirectedEdge thisEdge = this.sortedEdges.get(i);
            DirectedEdge anotherEdge = another.sortedEdges.get(i);

            int comparisonResult = thisEdge.compareTo(anotherEdge);
            if (comparisonResult != 0)
                return comparisonResult;
        }

        return 0;
    }

    @Override
    protected Map<String, Object> buildCycleAsJson() {
        Map<String, Object> cycleAsJson = super.buildCycleAsJson();
        cycleAsJson.put("sortedEdges", buildEdgesAsJson(sortedEdges));
        return cycleAsJson;
    }
}
