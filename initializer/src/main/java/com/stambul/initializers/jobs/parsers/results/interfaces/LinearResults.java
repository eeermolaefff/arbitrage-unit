package com.stambul.initializers.jobs.parsers.results.interfaces;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.Pair;
import org.springframework.core.ResolvableType;

import java.util.*;

public abstract class LinearResults<P extends DTO<P>> implements ParsingResult {
    protected final Class<P> primaryClass;
    protected final Set<P> primaryResults = new TreeSet<>();
    protected Exception interruptedException = null;
    protected int executedTaskSize = 0;

    protected LinearResults(Class<P> primaryClass) {
        this.primaryClass = primaryClass;
    }

    public void add(P primary) {
        if (primary == null)
            throw new IllegalArgumentException("Argument should not be null: primary=null");
        primaryResults.add(primary);
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
    public void block(Identifiable object, Exception exception) {}

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
        return new LinkedList<>();
    }

    @Override
    public List<Pair<Identifiable, Exception>> blockAllResults(Exception cause) {
        //TODO make normal
        List<Pair<Identifiable, Exception>> list = new LinkedList<>();
        for (P obj : primaryResults)
            list.add(new Pair<>(obj, cause));
        return list;
    };

    @Override
    public boolean isParsingResultEmpty() {
        return primaryResults.isEmpty();
    }

    @Override
    public String toString() {
        String format = "%s[primaryResults=%s, interruptedException=%s, executedTaskSize=%d]";
        return String.format(
                format, this.getClass().getSimpleName(), primaryResults, interruptedException, executedTaskSize
        );
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClass(primaryClass)
        );
    }
    
    public Set<P> getParsingResults() {
        return primaryResults;
    }
}
