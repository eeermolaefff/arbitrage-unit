package com.stambul.arbitrageur.arbitrage.graph.interfaces;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.library.database.objects.dto.CurrencyDTO;

public interface WeightedDigraph {
    int getNumberOfVertices();
    int getNumberOfEdges();
    Iterable<DirectedEdge> getAdjacentEdges(int vertex);
    CurrencyDTO getCurrency(int vertex);
    double getWeight(int from, int to);
    DirectedEdge getEdge(int from, int to);
    void add(DirectedEdge edge);
    void addAll(Iterable<DirectedEdge> edges);
    Iterable<DirectedEdge> getAllEdges();
    ArrayBasedDigraph reversed();
}
