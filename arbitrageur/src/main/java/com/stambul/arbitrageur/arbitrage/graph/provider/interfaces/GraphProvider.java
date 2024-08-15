package com.stambul.arbitrageur.arbitrage.graph.provider.interfaces;

import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;

public interface GraphProvider {
    WeightedDigraph makeGraph();
    VerticesEncoder getEncoder();
}
