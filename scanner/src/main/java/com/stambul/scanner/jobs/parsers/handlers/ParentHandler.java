package com.stambul.scanner.jobs.parsers.handlers;

import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.events.JobFinishEvent;
import com.stambul.scanner.jobs.events.ParserFinishEvent;
import com.stambul.scanner.jobs.events.RebootEvent;
import com.stambul.scanner.services.BlacklistService;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.library.tools.Pair;
import com.stambul.scanner.services.DatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParentHandler {
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final BlacklistService blacklistService;
    protected final ApplicationEventPublisher publisher;
    protected final DatabaseService databaseService;

    @Autowired
    public ParentHandler(
            DatabaseService databaseService,
            ApplicationEventPublisher publisher,
            BlacklistService blacklistService
    ) {
        this.databaseService = databaseService;
        this.publisher = publisher;
        this.blacklistService = blacklistService;
    }


    @EventListener
    @Async("listenersExecutor")
    public void handleFinishEvent(ParserFinishEvent<?> parserFinishEvent) {
        ParsingResults<?> results = parserFinishEvent.getParsingResults();

        if (results.isEmpty()) {
            String message = "Empty parsing results has received while parsing: %s -> %s";
            logger.warn(String.format(message, parserFinishEvent.getSource(), parserFinishEvent));
            return;
        }

        if (!results.isBlacklistEmpty()) {
            for (int marketId : results.getMarketIds()) {
                Iterable<Pair<String, Exception>> blacklist = results.getBlacklist(marketId);
                logger.debug(String.format("BLOCK(%s): %s", results.getParserName(), cut(blacklist.toString(), 500)));
                blacklistService.addObjectsToBlacklist(results.getParserName(), blacklist);
            }
        }

        ApplicationEvent event;
        try {
            databaseService.insertParsingResults(results);
            event = new JobFinishEvent(this, parserFinishEvent.getJobClass());
        } catch (Exception e) {
            RuntimeException exception = new RuntimeException("Can not insert swaps to database", e);
            event = new RebootEvent(this, parserFinishEvent.getJobClass(), exception);
        }
        publisher.publishEvent(event);
    }

    private String cut(String message, int length) {
        if (message.length() > length)
            message = message.substring(0, length) + "[...]";
        return message;
    }
}

