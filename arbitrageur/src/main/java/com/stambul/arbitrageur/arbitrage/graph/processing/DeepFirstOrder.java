package com.stambul.arbitrageur.arbitrage.graph.processing;

import com.stambul.arbitrageur.arbitrage.graph.edges.interfaces.DirectedEdge;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class DeepFirstOrder {
    private boolean[] marked;
    private Stack<Integer> reversePost;
    private Queue<Integer> post;
    private Queue<Integer> pre;

    public DeepFirstOrder(WeightedDigraph G) {
        pre = new LinkedList<>();
        post = new LinkedList<>();
        reversePost = new Stack<>();
        marked = new boolean[G.getNumberOfVertices()];
        for (int from = 0; from < G.getNumberOfVertices(); from++)
            if (!marked[from])
                dfs(G, from);
    }

    private void dfs(WeightedDigraph G, int from) {
        marked[from] = true;
        pre.add(from);
        for (DirectedEdge e : G.getAdjacentEdges(from)) {
            int to = e.getVertexTo();
            if (!marked[to])
                dfs(G, to);
        }
        post.add(from);
        reversePost.push(from);
    }

    public Iterable<Integer> pre() {
        return pre;
    }
    public Iterable<Integer> post() {
        return post;
    }
    public Iterable<Integer> reversePost() {
        return reversePost;
    }
}
