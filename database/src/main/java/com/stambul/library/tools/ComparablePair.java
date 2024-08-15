package com.stambul.library.tools;

public class ComparablePair<F, S extends Comparable<S>> extends Pair<F, S> implements Comparable<ComparablePair<F, S>> {
    public ComparablePair(F first, S second) {
        super(first, second);
    }

    @Override
    public int compareTo(ComparablePair<F, S> another) {
        return this.getSecond().compareTo(another.getSecond());
    }
}
