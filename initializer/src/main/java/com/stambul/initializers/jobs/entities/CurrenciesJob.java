package com.stambul.initializers.jobs.entities;

import com.stambul.initializers.jobs.entities.interfaces.ParentJob;
import com.stambul.initializers.jobs.parsers.handlers.ContractsParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.CurrenciesParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.ContractsParsersResults;
import com.stambul.initializers.jobs.parsers.results.CurrenciesParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;


@Service
public class CurrenciesJob extends ParentJob<CurrenciesJob, CurrenciesParsersHandler, CurrenciesParsersResults> {

    @Autowired
    public CurrenciesJob(
            ParsersHandler<CurrenciesParsersHandler, CurrenciesParsersResults> handler,
            ApplicationEventPublisher publisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${coinmarketcap.initialization.currencies.start.delay.iso}") String startDelayISO,
            @Value("${coinmarketcap.initialization.currencies.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${coinmarketcap.initialization.currencies.repeat.delay.iso}") String repeatDelayISO,
            @Value("${coinmarketcap.initialization.currencies.launch}") boolean launchFlag,
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
