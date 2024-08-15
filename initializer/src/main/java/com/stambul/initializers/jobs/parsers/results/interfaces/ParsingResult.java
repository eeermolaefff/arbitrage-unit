package com.stambul.initializers.jobs.parsers.results.interfaces;

import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.Pair;
import org.springframework.core.ResolvableTypeProvider;

import java.util.List;

public interface ParsingResult extends ResolvableTypeProvider {
    void taskDone();
    void block(Identifiable object, Exception exception);
    int getExecutedTaskSize();
    void setInterruptedException(Exception exception);
    Exception getInterruptedException();
    List<Pair<Identifiable, Exception>> getBlockingResults();
    List<Pair<Identifiable, Exception>> blockAllResults(Exception cause);
    boolean isParsingResultEmpty();
}
