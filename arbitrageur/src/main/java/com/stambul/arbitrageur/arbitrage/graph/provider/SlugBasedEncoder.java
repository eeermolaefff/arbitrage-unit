package com.stambul.arbitrageur.arbitrage.graph.provider;

import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;
import com.stambul.library.tools.StringST;

import java.util.Map;
import java.util.TreeMap;

public class SlugBasedEncoder implements VerticesEncoder {
    private final StringST<Integer> keyToIndex = new StringST<>();
    private final Map<Integer, String> indexToKey = new TreeMap<>();

    public String getCode(int index) {
        return indexToKey.get(index);
    }

    public int makeIfNotExistsIndex(String currency, String market) {
        return encode(key(currency, market));
    }

    public Integer getIndex(String blockchain, String address) {
        return keyToIndex.get(key(blockchain, address));
    }

    public int size() {
        return keyToIndex.size();
    }

    private int encode(String key) {
        Integer index = keyToIndex.get(key);
        if (index == null) {
            index = keyToIndex.size();
            keyToIndex.put(key, index);
            indexToKey.put(index, key);
        }
        return index;
    }

    private String key(String currency, String market) {
        if (currency == null || market == null) {
            String message = "currency and market objects shouldn't be null: currency=%s, market=%s";
            throw new IllegalArgumentException(String.format(message, currency, market));
        }

        return currency;
//        return market + "." + currency;
    }
}
