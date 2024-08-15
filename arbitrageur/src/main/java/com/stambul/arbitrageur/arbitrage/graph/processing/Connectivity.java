package com.stambul.arbitrageur.arbitrage.graph.processing;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;

import java.util.LinkedList;
import java.util.List;

public class Connectivity {
    private final VerticesEncoder encoder;
    private final boolean[] marked;
    private final int[] id;
    private int numberOfComponents;

    public Connectivity(WeightedDigraph G, VerticesEncoder encoder) {
        this.encoder = encoder;
        marked = new boolean[G.getNumberOfVertices()];
        id = new int[G.getNumberOfVertices()];
        DeepFirstOrder dfs = new DeepFirstOrder(G.reversed());
        for (int from :  dfs.reversePost())
            if (!marked[from]) {
                dfs(G, from);
                numberOfComponents++;
            }
    }

    public int getSize() {
        return marked.length;
    }

    public boolean isStronglyConnected(int v, int w) {
        return id[v] == id[w];
    }

    public int getComponentIdByVertex(int v) {
        return id[v];
    }

    public int getNumberOfComponents() {
        return numberOfComponents;
    }

    public Iterable<Integer> getComponentByVertex(int v) {
        int componentId = id[v];
        return getComponentById(componentId);
    }

    public int getComponentSizeById(int componentId) {
        int size = 0;
        for (int id : this.id)
            if (id == componentId)
                size++;
        return size;
    }

    public Iterable<Integer> getComponentById(int componentId) {
        List<Integer> component = new LinkedList<>();
        for (int v = 0; v < id.length; v++)
            if (id[v] == componentId)
                component.add(v);
        return component;
    }

    public Iterable<Integer>[] getAllComponents() {
        List<Integer>[] components = new LinkedList[numberOfComponents];
        for (int i = 0; i < numberOfComponents; i++)
            components[i] = new LinkedList<>();

        for (int v = 0; v < id.length; v++)
            components[id[v]].add(v);

        return components;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Iterable<Integer> component : getAllComponents()) {
            builder.append("[");
            for (int vertex : component) {
                builder.append(encoder.getCode(vertex)).append(" ");
            }
            builder.deleteCharAt(builder.length() - 1).append("]\n");
        }
        return builder.toString();
    }

    private void dfs(WeightedDigraph G, int from) {
        id[from] = numberOfComponents;
        marked[from] = true;
        for (DirectedEdge edge : G.getAdjacentEdges(from)) {
            int to = edge.getVertexTo();
            if (!marked[to])
                dfs(G, to);
        }
    }
}
