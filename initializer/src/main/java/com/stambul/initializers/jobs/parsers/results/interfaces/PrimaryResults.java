package com.stambul.initializers.jobs.parsers.results.interfaces;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.Pair;
import org.springframework.core.ResolvableType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class PrimaryResults<P extends DTO<P>> implements ParsingResult {
    protected final Class<P> primaryClass;
    protected final Map<Integer, P> primaryMap = new TreeMap<>();
    protected final Map<Integer, Identifiable> idBlockedMap = new TreeMap<>();
    protected final Map<Integer, Exception> blockedExceptionMap = new TreeMap<>();
    protected Exception interruptedException = null;
    protected int executedTaskSize = 0;

    protected PrimaryResults(Class<P> primaryClass) {
        this.primaryClass = primaryClass;
    }

    public void add(P primary) {
        if (primary == null)
            throw new IllegalArgumentException("Argument should not be null: primary=null");

        primaryMap.put(primary.getId(), primary);
    }

    @Override
    public void taskDone() {
        executedTaskSize++;
    }

    @Override
    public int getExecutedTaskSize() {
        return executedTaskSize;
    }

    @Override
    public void block(Identifiable object, Exception exception) {
        int objectId = object.getId();

        idBlockedMap.put(objectId, object);
        blockedExceptionMap.put(objectId, exception);
    }

    @Override
    public void setInterruptedException(Exception interruptedException) {
        this.interruptedException = interruptedException;
    }

    @Override
    public Exception getInterruptedException() {
        return interruptedException;
    }

    @Override
    public List<Pair<Identifiable, Exception>> getBlockingResults() {
        List<Pair<Identifiable, Exception>> results = new LinkedList<>();

        for (int id : idBlockedMap.keySet()) {
            Identifiable blockedObject = idBlockedMap.get(id);
            Exception cause = blockedExceptionMap.get(id);
            results.add(new Pair<>(blockedObject, cause));
        }

        return results;
    }

    @Override
    public List<Pair<Identifiable, Exception>> blockAllResults(Exception cause) {
        List<Pair<Identifiable, Exception>> results = new LinkedList<>();

        for (int id : primaryMap.keySet()) {
            Identifiable blockedObject = primaryMap.get(id);
            results.add(new Pair<>(blockedObject, cause));
        }

        return results;
    };

    @Override
    public boolean isParsingResultEmpty() {
        return primaryMap.isEmpty();
    }

    @Override
    public String toString() {
        String format = "%s[primaryMap=%s, idBlockedMap=%s, blockedExceptionMap=%s, " +
                "interruptedException=%s, executedTaskSize=%d]";
        return String.format(
                format, this.getClass().getSimpleName(), primaryMap, idBlockedMap,
                blockedExceptionMap, interruptedException, executedTaskSize
        );
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClass(primaryClass)
        );
    }

    public Map<Integer, P> getPrimaryMap() {
        return primaryMap;
    }
}
