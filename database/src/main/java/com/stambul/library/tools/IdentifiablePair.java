package com.stambul.library.tools;

import com.stambul.library.database.objects.interfaces.Identifiable;

public class IdentifiablePair<F extends Identifiable, S> extends Pair<F, S> implements Identifiable {
    public IdentifiablePair(F first, S second) {
        super(first, second);
    }

    @Override
    public Integer getId() {
        return getFirst().getId();
    }
}
