package com.stambul.initializers.jobs.parsers.handlers.interfaces;

import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.parsers.results.interfaces.ParsingResult;

public interface ParsersHandler<H extends ParsersHandler<H, P>, P extends ParsingResult> {
    int launchParsers();
    void reboot();
    void handleParsingResults(ParserFinishEvent<H, P> finishEvent);
}
