package com.stambul.initializers.jobs.entities;

import com.stambul.initializers.jobs.entities.interfaces.ParentJob;
import com.stambul.initializers.jobs.parsers.handlers.CurrenciesParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.MarketsParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.CurrenciesParsersResults;
import com.stambul.initializers.jobs.parsers.results.MarketsParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class MarketsJob extends ParentJob<MarketsJob, MarketsParsersHandler, MarketsParsersResults> {

    @Autowired
    public MarketsJob(
            ParsersHandler<MarketsParsersHandler, MarketsParsersResults> handler,
            ApplicationEventPublisher publisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${coinmarketcap.initialization.markets.start.delay.iso}") String startDelayISO,
            @Value("${coinmarketcap.initialization.markets.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${coinmarketcap.initialization.markets.repeat.delay.iso}") String repeatDelayISO,
            @Value("${coinmarketcap.initialization.markets.launch}") boolean launchFlag,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(
                handler, publisher, scheduler, startDelayISO, scheduleDelayISO,
                repeatDelayISO, launchFlag, maxMessageLength
        );
    }

    @Override
    protected List<Class> initRequirementsList() {
        return new LinkedList<>();
    }
}
