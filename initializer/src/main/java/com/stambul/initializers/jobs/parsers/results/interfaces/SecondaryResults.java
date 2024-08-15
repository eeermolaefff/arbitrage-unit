package com.stambul.initializers.jobs.parsers.results.interfaces;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.IdentifiablePair;
import com.stambul.library.tools.Pair;
import org.springframework.core.ResolvableType;

import java.util.*;

public abstract class SecondaryResults<P extends DTO<P>, S extends DTO<S>> extends PrimaryResults<P> {
    protected final Class<S> secondaryClass;
    protected final Map<Integer, Set<S>> secondaryMap = new TreeMap<>();

    protected SecondaryResults(Class<P> primaryClass, Class<S> secondaryClass) {
        super(primaryClass);
        this.secondaryClass = secondaryClass;
    }

    public void add(P primary, S secondary) {
        add(primary);

        if (secondary == null)
            throw new IllegalArgumentException("Argument should not be null: secondary=null");

        secondaryMap.computeIfAbsent(primary.getId(), x -> new TreeSet<>()).add(secondary);
    }

    public void add(P primary, Collection<? extends S> secondary) {
        add(primary);

        if (secondary == null)
            throw new IllegalArgumentException("Argument should not be null: secondary=null");

        secondaryMap.computeIfAbsent(primary.getId(), x -> new TreeSet<>()).addAll(secondary);
    }

    public Map<Integer, Set<S>> getSecondaryMap() {
        return secondaryMap;
    }

    @Override
    public String toString() {
        String format = "%s[secondaryMap=%s, primaryMap=%s, idBlockedMap=%s, blockedExceptionMap=%s, " +
                "interruptedException=%s, executedTaskSize=%d]";
        return String.format(
                format, this.getClass().getSimpleName(), secondaryMap, primaryMap,
                idBlockedMap, blockedExceptionMap, interruptedException, executedTaskSize
        );
    }

    @Override
    public List<Pair<Identifiable, Exception>> blockAllResults(Exception cause) {
        List<Pair<Identifiable, Exception>> results = new LinkedList<>();

        for (P primary : primaryMap.values()) {
            Set<S> secondary = secondaryMap.get(primary.getId());
            results.add(new Pair<>(new IdentifiablePair<>(primary, secondary), cause));
        }

        return results;
    };


    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClassWithGenerics(
                        primaryClass, ResolvableType.forClass(secondaryClass)
                )
        );
    }

}
