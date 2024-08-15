package com.stambul.arbitrageur.arbitrage.cycles.objects;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.*;

public class ConnectedCycle extends ProfitComparableCycle {
    private final Map<Integer, List<DirectedEdge>> connections = new TreeMap<>();
    private final Map<Integer, Double> startStakes = new TreeMap<>();

    public ConnectedCycle(List<DirectedEdge> cycle, double startCurrencyAmount) {
        super(cycle, startCurrencyAmount);
    }

    public void connect(int vertexFrom, double startStake, List<DirectedEdge> path) {
        if (path == null || path.isEmpty())
            throw new IllegalArgumentException("Invalid connection: path=" + path);
        connections.put(vertexFrom, path);
        startStakes.put(vertexFrom, startStake);
    }

    public int getNumberOfConnections() {
        return connections.size();
    }

    public Set<Integer> getConnectedVertices() {
        return connections.keySet();
    };

    public List<DirectedEdge> getConnection(int vertexFrom) {
        return connections.get(vertexFrom);
    };

    public Iterable<List<DirectedEdge>> getAllConnections() {
        return new LinkedList<>(connections.values());
    };

    @Override
    protected Map<String, Object> buildCycleAsJson() {
        Map<String, Object> cycleAsJson = super.buildCycleAsJson();
        cycleAsJson.put("connections", buildConnectionsList());
        return cycleAsJson;
    }

    private List<Object> buildConnectionsList() {
        List<Object> connectionsList = new LinkedList<>();
        for (int vertex : connections.keySet()) {
            double startStake = startStakes.get(vertex);
            List<DirectedEdge> connection = connections.get(vertex);
            connectionsList.add(buildEdgesAsJson(connection, startStake));
        }
        return connectionsList;
    }
}
