package com.stambul.arbitrageur.arbitrage.graph.provider;

import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;
import com.stambul.library.tools.StringST;

import java.util.Map;
import java.util.TreeMap;

public class AddressBasedEncoder implements VerticesEncoder {
    private final StringST<Integer> keyToIndex = new StringST<>();
    private final Map<Integer, String> indexToKey = new TreeMap<>();

    public String getCode(int index) {
        return indexToKey.get(index);
    }

    public Integer getIndex(String blockchain, String address) {
        return keyToIndex.get(key(blockchain, address));
    }

    public int makeIfNotExistsIndex(String blockchain, String address) {
        return encode(key(blockchain, address));
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

    private String key(String blockchain, String address) {
        if (blockchain == null || address == null) {
            String message = "Blockchain and address objects shouldn't be null: blockchain=%s, address=%s";
            throw new IllegalArgumentException(String.format(message, blockchain, address));
        }
        return key(blockchain) + "." + address.toLowerCase();
    }

    private String key(String field) {
        return field.toLowerCase().replaceAll("[-() ]+", "_");
    }
}
