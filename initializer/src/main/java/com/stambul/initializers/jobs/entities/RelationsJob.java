package com.stambul.initializers.jobs.entities;

import com.stambul.initializers.jobs.entities.interfaces.ParentJob;
import com.stambul.initializers.jobs.parsers.handlers.RelationsParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.RelationsParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationsJob extends ParentJob<RelationsJob, RelationsParsersHandler, RelationsParsersResults> {

    @Autowired
    public RelationsJob(
            ParsersHandler<RelationsParsersHandler, RelationsParsersResults> handler,
            ApplicationEventPublisher publisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${coinmarketcap.initialization.relations.start.delay.iso}") String startDelayISO,
            @Value("${coinmarketcap.initialization.relations.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${coinmarketcap.initialization.relations.repeat.delay.iso}") String repeatDelayISO,
            @Value("${coinmarketcap.initialization.relations.launch}") boolean launchFlag,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(
                handler, publisher, scheduler, startDelayISO, scheduleDelayISO,
                repeatDelayISO, launchFlag, maxMessageLength
        );
    }

    @Override
    protected List<Class> initRequirementsList() {
        return List.of(
                CurrenciesJob.class,
                MarketsJob.class
        );
    }
}
