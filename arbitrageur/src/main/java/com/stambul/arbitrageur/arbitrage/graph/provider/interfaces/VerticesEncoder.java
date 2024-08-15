package com.stambul.arbitrageur.arbitrage.graph.provider.interfaces;

public interface VerticesEncoder {
    String getCode(int index);
    int makeIfNotExistsIndex(String first, String second);
    Integer getIndex(String first, String second);
    int size();
}
