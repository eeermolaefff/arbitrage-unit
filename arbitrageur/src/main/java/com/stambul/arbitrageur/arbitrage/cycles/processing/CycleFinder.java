package com.stambul.arbitrageur.arbitrage.cycles.processing;

import com.stambul.arbitrageur.arbitrage.graph.interfaces.ArrayBasedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;

import java.util.LinkedList;

public class CycleFinder {
    private final boolean[] marked;
    private final boolean[] onStack;
    private final DirectedEdge[] edgeTo;
    private LinkedList<DirectedEdge> cyclePath;

    public CycleFinder(ArrayBasedDigraph G) {
        marked = new boolean[G.getNumberOfVertices()];
        onStack = new boolean[G.getNumberOfVertices()];
        edgeTo = new DirectedEdge[G.getNumberOfVertices()];
        for (int vertex = 0; vertex < G.getNumberOfVertices(); vertex++)
            if (!marked[vertex])
                dfs(G, vertex);
    }

    private void dfs(ArrayBasedDigraph G, int from) {    //Deep-First Search
        marked[from] = true;
        onStack[from] = true;
        for (DirectedEdge edge : G.getAdjacentEdges(from)) {
            int to = edge.getVertexTo();
            if (hasCycle())
                return;
            else if (!marked[to]) {
                edgeTo[to] = edge;
                dfs(G, to);
            }
            else if (onStack[to]) {
                cyclePath = new LinkedList<>();
                edgeTo[to] = null;
                cyclePath.push(edge);
                for (DirectedEdge x = edgeTo[from]; x != null; x = edgeTo[x.getVertexFrom()])
                    cyclePath.push(x);
                return;
            }
        }
        onStack[from] = false;
    }

    public boolean hasCycle() {
        return cyclePath != null;
    }

    public LinkedList<DirectedEdge> getCycle() {
        return cyclePath;
    }
}
