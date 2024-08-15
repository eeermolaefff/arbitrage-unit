package com.stambul.initializers.jobs.entities;

import com.stambul.initializers.jobs.entities.interfaces.ParentJob;
import com.stambul.initializers.jobs.parsers.handlers.ContractsParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.ContractsParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContractsJob extends ParentJob<ContractsJob, ContractsParsersHandler, ContractsParsersResults> {
    @Autowired
    public ContractsJob(
            ParsersHandler<ContractsParsersHandler, ContractsParsersResults> handler,
            ApplicationEventPublisher publisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${coinmarketcap.initialization.contracts.start.delay.iso}") String startDelayISO,
            @Value("${coinmarketcap.initialization.contracts.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${coinmarketcap.initialization.contracts.repeat.delay.iso}") String repeatDelayISO,
            @Value("${coinmarketcap.initialization.contracts.launch}") boolean launchFlag,
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
