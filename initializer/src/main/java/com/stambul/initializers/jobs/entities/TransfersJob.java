package com.stambul.initializers.jobs.entities;

import com.stambul.initializers.jobs.entities.interfaces.ParentJob;
import com.stambul.initializers.jobs.parsers.handlers.TransfersParsersHandler;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.TransferParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransfersJob extends ParentJob<TransfersJob, TransfersParsersHandler, TransferParsersResults> {
    @Autowired
    public TransfersJob(
            ParsersHandler<TransfersParsersHandler, TransferParsersResults> handler,
            ApplicationEventPublisher publisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${coinmarketcap.initialization.transfers.start.delay.iso}") String startDelayISO,
            @Value("${coinmarketcap.initialization.transfers.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${coinmarketcap.initialization.transfers.repeat.delay.iso}") String repeatDelayISO,
            @Value("${coinmarketcap.initialization.transfers.launch}") boolean launchFlag,
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
                RelationsJob.class,
                ContractsJob.class
        );
    }
}
