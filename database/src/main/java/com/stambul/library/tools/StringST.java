package com.stambul.library.tools;

import java.util.LinkedList;
import java.util.Queue;

public class StringST<Data> {   // String Search Tree
    private Node<Data> root;
    private int size = 0;
    private static final int breadth = 256;
    private static class Node<Data> {
        private Data val = null;
        private final Node<Data>[] next = new Node[breadth];
    }
    public StringST() {
        root = null;
    }

    /**
     * @param key      Search key (converted to a string)
     * @return         The value associated with the key or null if the key hasn't found*/
    public Data get(Object key) {
        Node<Data> x = get(root, key.toString(), 0);
        if (x == null) return null;
        return x.val;
    }

    /** Changes the value associated with a specific key
     * @param key           Search key (converted to a string)
     * @param newValue      New value for the key association
     * @return              True if the value has been changed and False if the key hasn't found*/
    public boolean change(String key, Data newValue) {
        boolean found = contains(key);
        if (found)  root = put(root, key, newValue, 0);
        return found;
    }

    /** Adds an associated value to the key
     * @param key       Search key (converted to a string)
     * @param value     Associated value for the key
     * @return          True if the value has been added and False if an associated value already exists with this key*/
    public boolean put(Object key, Data value) {
        if (key == null)
            return false;

        boolean found = contains(key);
        if (!found) {
            size++;
            root = put(root, key.toString(), value, 0);
        }
        return !found;
    }

    /**
     * @param key       Search key (converted to a string)
     * @return          True if there is an associated value with the key and False otherwise*/
    public boolean contains(Object key) {
        return get(key.toString()) != null;
    }

    /**
     * @return          The number of associative pairs*/
    public int size() {
        return size;
    }

    /**
     * @return          The list of keys for which associated values have been added*/
    public LinkedList<String> keys() {
        return keysWithPrefix("");
    }

    /**
     * @param prefix    Prefix for a match
     * @return          The list of keys whose prefix matches the one you are looking for*/
    public LinkedList<String> keysWithPrefix(String prefix) {
        LinkedList<String> queue = new LinkedList<>();
        collect(get(root, prefix, 0), prefix, queue);
        return queue;
    }

    /**
     * @return  The longest key that is the prefix of the argument or "" if the appropriate key hasn't found*/
    public String longestPrefixOf(String string) {
        int length = search(root, string, 0, 0);
        return string.substring(0, length);
    }

    /**Deletes the value associated with the key
     * @param key   Key to be deleted (converted to a string)*/
    public void delete(Object key) {
        root = delete(root, (String) key, 0);
    }

    private Node<Data> get(Node<Data> currentNode, String key, int depth) {
        if (currentNode == null)
            return null;
        if (key.length() == depth)
            return currentNode;

        char symbol = key.charAt(depth);
        return get(currentNode.next[symbol], key, ++depth);
    }

    private Node<Data> put(Node<Data> currentNode, String key, Data val, int depth) {
        if (currentNode == null)
            currentNode = new Node<>();

        if (key.length() == depth) {
            currentNode.val = val;
            return currentNode;
        }

        char symbol = key.charAt(depth);
        currentNode.next[symbol] = put(currentNode.next[symbol], key, val, depth+1);
        return currentNode;
    }

    private void collect(Node<Data> startNode, String prefix, Queue<String> queue) {
        if (startNode == null) return;
        if (startNode.val != null)
            queue.add(prefix);
        for (char c = 0; c < breadth; c++)
            collect(startNode.next[c], prefix + c, queue);
    }

    private int search(Node<Data> node, String key, int depth, int length) {
        if (node == null) return length;
        if (node.val != null) length = depth;
        if (depth == key.length()) return length;

        char symbol = key.charAt(depth);
        return search(node.next[symbol], key, depth+1, length);
    }

    private boolean isAnyNextKey(Node<Data> x) {
        boolean isAnyKey = false;
        for (int i = 0; i < breadth; i++)
            if (x.next[i] != null) {
                isAnyKey = true;
                break;
            }
        return isAnyKey;
    }

    private Node<Data> delete(Node<Data> x, String key, int depth) {
        if (x == null) return null;
        if (key.length() == depth)
            x.val = null;
        else {
            char c = key.charAt(depth);
            x.next[c] = delete(x.next[c], key, depth+1);
        }
        if (x.val == null && !isAnyNextKey(x)) return null;
        return x;
    }
}


