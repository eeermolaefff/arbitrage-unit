package com.stambul.arbitrageur.arbitrage.cycles.processing;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.ArrayBasedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;

import java.util.*;

public class CycleSplitter {
    private final Map<Integer, Integer> oldToNew = new TreeMap<>();
    private final int[] newToOld;
    private final List<DirectedEdge>[] backwardEdges;
    private final boolean[] wasVisited;
    private final WeightedDigraph graph;
    private final DirectedEdge[] forwardEdges;

    public CycleSplitter(Collection<DirectedEdge> cycle) {
        if (cycle.isEmpty())
            throw new IllegalArgumentException("Empty cycle received: cycle=" + cycle);

        for (DirectedEdge edge : cycle) {
            int vertexOld = edge.getVertexFrom();
            int vertexNew = oldToNew.size();
            oldToNew.putIfAbsent(vertexOld, vertexNew);
        }

        forwardEdges = new DirectedEdge[oldToNew.size()];
        wasVisited = new boolean[oldToNew.size()];
        backwardEdges = new List[oldToNew.size()];

        newToOld = new int[oldToNew.size()];
        for (int vertexOld : oldToNew.keySet())
            newToOld[oldToNew.get(vertexOld)] = vertexOld;

        graph = buildGraph(cycle);
    }

    public Iterable<List<DirectedEdge>> split() {
        dfs(0);

        List<List<DirectedEdge>> cycles = new LinkedList<>();
        for (List<DirectedEdge> edges : backwardEdges) {
            if (edges == null)
                continue;
            for (DirectedEdge backwardEdge : edges)
                cycles.add(buildCycle(backwardEdge));
        }
        return cycles;
    }

    private List<DirectedEdge> buildCycle(DirectedEdge backwardEdge) {
        List<DirectedEdge> cycle = new LinkedList<>();

        int backwardFrom = backwardEdge.getVertexFrom();
        int backwardTo = backwardEdge.getVertexTo();
        cycle.add(backwardEdge.copy(newToOld[backwardFrom], newToOld[backwardTo]));

        for (int from = backwardTo, to; from != backwardFrom; from = to) {
            DirectedEdge next = forwardEdges[from];
            to = next.getVertexTo();
            cycle.add(next.copy(newToOld[from], newToOld[to]));
        }

        return cycle;
    }

    private WeightedDigraph buildGraph(Iterable<DirectedEdge> cycle) {
        WeightedDigraph graph = new ArrayBasedDigraph(oldToNew.size());
        for (DirectedEdge oldEdge : cycle) {
            int from = oldToNew.get(oldEdge.getVertexFrom());
            int to = oldToNew.get(oldEdge.getVertexTo());
            graph.add(oldEdge.copy(from, to));
        }
        return graph;
    }

    private void dfs(int vertexFrom) {
        wasVisited[vertexFrom] = true;

        for (DirectedEdge edge : graph.getAdjacentEdges(vertexFrom)) {
            int vertexTo = edge.getVertexTo();

            if (wasVisited[vertexTo]) {
                if (backwardEdges[vertexFrom] == null)
                    backwardEdges[vertexFrom] = new LinkedList<>();
                backwardEdges[vertexFrom].add(edge);
            } else {
                forwardEdges[vertexFrom] = edge;
                dfs(vertexTo);
            }
        }
    }
}
