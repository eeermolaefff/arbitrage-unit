package com.stambul.arbitrageur.arbitrage.graph.interfaces;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.library.database.objects.dto.CurrencyDTO;

import java.util.LinkedList;
import java.util.List;

public class ArrayBasedDigraph implements WeightedDigraph {
    private final int numberOfVertices;
    private int numberOfEdges;
    private final LinkedList<DirectedEdge>[] adjacentEdges;
    private final CurrencyDTO[] currencies;

    public ArrayBasedDigraph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        adjacentEdges = (LinkedList<DirectedEdge>[]) new LinkedList[numberOfVertices];
        currencies = new CurrencyDTO[numberOfVertices];
        for (int v = 0; v < numberOfVertices; v++)
            adjacentEdges[v] = new LinkedList<>();
    }

    @Override
    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    @Override
    public int getNumberOfEdges() {
        return numberOfEdges;
    }

    @Override
    public Iterable<DirectedEdge> getAdjacentEdges(int vertex) {
        return adjacentEdges[vertex];
    }

    @Override
    public CurrencyDTO getCurrency(int vertex) {
        return currencies[vertex];
    }

    @Override
    public double getWeight(int from, int to) {
        for (DirectedEdge e : adjacentEdges[from])
            if (e.getVertexTo() == to)
                return e.getWeight();
        return -1;
    }

    @Override
    public DirectedEdge getEdge(int from, int to) {
        for (DirectedEdge edge : adjacentEdges[from])
            if (edge.getVertexTo() == to)
                return edge;
        return null;
    }

    @Override
    public void add(DirectedEdge edge) {
        adjacentEdges[edge.getVertexFrom()].add(edge);
        currencies[edge.getVertexFrom()] = edge.getCurrencyFrom();
        currencies[edge.getVertexTo()] = edge.getCurrencyTo();
        numberOfEdges++;
    }

    @Override
    public void addAll(Iterable<DirectedEdge> edges) {
        if (edges == null) return;
        for (DirectedEdge edge : edges) {
            if (edge == null)
                throw new IllegalArgumentException("Null edge received");
            add(edge);
        };
    }

    @Override
    public Iterable<DirectedEdge> getAllEdges() {
        List<DirectedEdge> bag = new LinkedList<>();
        for (int v = 0; v < numberOfVertices; v++)
            for (DirectedEdge edge : getAdjacentEdges(v))
                bag.add(edge.reversed());
        return bag;
    }

    @Override
    public ArrayBasedDigraph reversed() {
        ArrayBasedDigraph reversed = new ArrayBasedDigraph(numberOfVertices);
        for (int from = 0; from < numberOfVertices; from++)
            for (DirectedEdge edge : adjacentEdges[from])
                reversed.add(edge.reversed());
        return reversed;
    }
}
