package com.stambul.arbitrageur.arbitrage.cycles.processing;

import com.stambul.arbitrageur.arbitrage.cycles.processing.CycleFinder;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.ArrayBasedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Arbitrage {
    private final DirectedEdge[] edgeTo;
    private final double[] distTo;
    private final boolean[] isInTheQueue;
    private LinkedList<DirectedEdge> cycle;
    private final Queue<Integer> workingQueue;
    private int numberOfIterations = 0;
    private final double additionalCommission, startStake;
    private final int startVertex, numberOfVertices;

    public Arbitrage(WeightedDigraph G, int startVertex, double additionalCommission, double startStake) {
        this.additionalCommission = additionalCommission;
        this.startStake = startStake;
        this.startVertex = startVertex;
        this.numberOfVertices = G.getNumberOfVertices();

        edgeTo = new DirectedEdge[numberOfVertices];
        distTo = new double[numberOfVertices];
        isInTheQueue = new boolean[numberOfVertices];
        workingQueue = new LinkedList<>();

        distTo[startVertex] = startStake;
        workingQueue.add(startVertex);
        isInTheQueue[startVertex] = true;

        while (!workingQueue.isEmpty() && !hasCycle()) {
            int vertex = workingQueue.poll();
            isInTheQueue[vertex] = false;
            relax(G, vertex);
        }
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public double getDistanceTo(int destinationVertex) {
        return distTo[destinationVertex];
    }

    public double convert(double startCurrencyAmount, int destinationVertex) {
        Iterable<DirectedEdge> path = getPathTo(destinationVertex);
        if (path == null)
            return 0;
        double destAmount = startCurrencyAmount;
        for (DirectedEdge edge : path)
            destAmount = edge.goThrough(destAmount);
        return destAmount;
    }

    public double getAdditionalCommission() {
        return additionalCommission;
    }

    public double getStartStake() {
        return startStake;
    }

    public int getStartVertex() {
        return startVertex;
    }

    public boolean hasPathTo(int dest) {
        return distTo[dest] != 0;
    }

    public List<DirectedEdge> getPathTo(int dest) {
        if (!hasPathTo(dest))
            return null;

        LinkedList<DirectedEdge> path = new LinkedList<>();
        for (DirectedEdge e = edgeTo[dest]; e != null; e = edgeTo[e.getVertexFrom()])
            path.addFirst(e);
        return path;
    }

    public List<DirectedEdge> getCycle() {
        return cycle;
    }

    public boolean hasCycle() {
        return cycle != null;
    }

    private void findCycle() {
        int numberOfVertices = edgeTo.length;
        ArrayBasedDigraph G = new ArrayBasedDigraph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++)
            if (edgeTo[i] != null)
                G.add(edgeTo[i]);
        CycleFinder cycleFinder = new CycleFinder(G);
        cycle = cycleFinder.getCycle();
    }

    private void relax(WeightedDigraph G, int from) {
        double initialAmount = distTo[from];

        for (DirectedEdge edge : G.getAdjacentEdges(from)) {
            if (hasCycle())
                return;
            if (!edge.withinTransactionLimits(distTo[edge.getVertexFrom()]))
                continue;

            double newDist = edge.goThrough(initialAmount) * additionalCommission;

            if (distTo[edge.getVertexTo()] < newDist)
                update(edge, newDist);

            if (numberOfIterations++ % G.getNumberOfVertices() == 0) {
                findCycle();
                if (hasCycle()) return;
            }
        }
    }

    private void update(DirectedEdge edge, double newDist) {
        int to = edge.getVertexTo();

        distTo[to] = newDist;
        edgeTo[to] = edge;
        if (!isInTheQueue[to]) {
            workingQueue.add(to);
            isInTheQueue[to] = true;
        }
    }
}
