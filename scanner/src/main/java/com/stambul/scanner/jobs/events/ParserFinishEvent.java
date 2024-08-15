package com.stambul.scanner.jobs.events;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.List;

public class ParserFinishEvent<D extends DTO<D>> extends ApplicationEvent implements ResolvableTypeProvider {
    private final int marketId;
    private final ParsingResults<D> parsingResults;
    private final Class<D> dataClass;
    private final Class<?> jobClass;

    public ParserFinishEvent(
            Object source,
            Class<?> jobClass,
            Class<D> dataClass,
            int marketId,
            ParsingResults<D> parsingResults
    ) {
        super(source);
        this.marketId = marketId;
        this.jobClass = jobClass;
        this.dataClass = dataClass;
        this.parsingResults = parsingResults;
    }

    public Class<?> getJobClass() {
        return jobClass;
    }

    public int getMarketId() {
        return marketId;
    }

    public ParsingResults<D> getParsingResults() {
        return parsingResults;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClass(dataClass)
        );
    }

    @Override
    public String toString() {
        String format = this.getClass().getSimpleName() + "[marketId=%s, parsingResults=%s]";
        return String.format(format, marketId, parsingResults);
    }
}
