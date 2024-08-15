package com.stambul.initializers.jobs.events;

import com.stambul.initializers.jobs.parsers.results.interfaces.ParsingResult;
import org.springframework.core.ResolvableType;

public class ParserFinishEvent<J, P extends ParsingResult> extends DirectMessage<J> {
    private final P parsingResults;

    public ParserFinishEvent(Object sender, Class<J> recipient, P parsingResults) {
        super(sender, recipient);
        this.parsingResults = parsingResults;
    }

    public P getParsingResults() {
        return parsingResults;
    };

    @Override
    protected String toStringAdditional() {
        return "parsingResults=" + parsingResults;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), recipient, parsingResults.getClass()
        );
    }
}