package com.stambul.arbitrageur.arbitrage.cycles.objects.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class Cycle<T> implements Comparable<T> {
    protected Integer id;
    protected final List<DirectedEdge> cycle;
    protected String stringRepresentation;

    public Cycle(List<DirectedEdge> cycle) {
        this.cycle = cycle;
    }

    public List<DirectedEdge> getCycle() {
        return cycle;
    }

    public void setId(int id) {
        this.id = id;
        if (stringRepresentation != null)
            stringRepresentation = null;
    }

    public Integer getId() {
        return id;
    }

    public int getCycleSize() {
        return cycle.size();
    }

    public List<DirectedEdge> getCycle(int firsElementPosition) {
        if (firsElementPosition == 0)
            return cycle;

        if (cycle == null || cycle.isEmpty())
            return  null;

        LinkedList<DirectedEdge> scrolled = new LinkedList<>();
        scrolled.addAll(cycle.subList(firsElementPosition, cycle.size()));
        scrolled.addAll(cycle.subList(0, firsElementPosition));
        return scrolled;
    }

    public int positionOf(int vertex) {
        int pos = 0;
        for (DirectedEdge edge : cycle) {
            if (edge.getVertexFrom() == vertex)
                return pos;
            pos++;
        }
        return -1;
    }

    protected Map<String, Object> buildCycleAsJson() {
        Map<String, Object> cycleAsJson = new TreeMap<>();

        cycleAsJson.put("id", id);
        cycleAsJson.put("className", this.getClass().getSimpleName());
        cycleAsJson.put("size", cycle.size());
        cycleAsJson.put("cycle", buildEdgesAsJson(cycle));

        return cycleAsJson;
    }

    protected List<Object> buildEdgesAsJson(Iterable<DirectedEdge> edges) {
        List<Object> list = new LinkedList<>();
        for (DirectedEdge edge : edges)
            list.add(edge.toJSON());
        return list;
    }

    protected List<Object> buildEdgesAsJson(Iterable<DirectedEdge> edges, double startStake) {
        List<Object> list = new LinkedList<>();
        for (DirectedEdge edge : edges) {
            list.add(edge.toJSON(startStake));
            startStake = edge.goThrough(startStake);
        }
        return list;
    }

    protected String toJSONString(Map<String, Object> json) {
        try {
            return new ObjectMapper().writeValueAsString(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Can not cast map to JSON string: [%s]", json), e);
        }
    }

    @Override
    public String toString() {
        if (stringRepresentation == null)
            stringRepresentation = toJSONString(buildCycleAsJson());
        return stringRepresentation;
    }
}
