package com.stambul.arbitrageur.jobs.entites;

import com.stambul.arbitrageur.arbitrage.graph.processing.GraphProcessor;
import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.graph.interfaces.WeightedDigraph;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.GraphProvider;
import com.stambul.arbitrageur.arbitrage.graph.provider.interfaces.VerticesEncoder;
import com.stambul.arbitrageur.jobs.entites.interfaces.Job;
import com.stambul.arbitrageur.jobs.events.ArbitrageurFinishEvent;
import com.stambul.library.database.interaction.services.ConnectionManager;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class ArbitrageurJob extends Job {

    private final GraphProvider graphProvider;
    private final GraphProcessor processor;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ArbitrageurJob(
            ApplicationEventPublisher eventPublisher,
            GraphProcessor processor,
            GraphProvider graphProvider,
            ConnectionManager connectionManager,
            @Qualifier("arbitrageScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${arbitrageur.repeat.delay.iso}") String repeatDelayISO,
            @Value("${arbitrageur.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${arbitrageur.start.delay.iso}") String startDelayISO,
            @Value("${arbitrageur.launch}") boolean launchFlag,
            @Value("${arbitrageur.console.log}") boolean logFlag
    ) {
        super(connectionManager, scheduler, repeatDelayISO, scheduleDelayISO, startDelayISO, launchFlag, logFlag);
        this.eventPublisher = eventPublisher;
        this.processor = processor;
        this.graphProvider = graphProvider;
    }

    @Override
    protected void bodyActions() {
        WeightedDigraph graph = graphProvider.makeGraph();
        VerticesEncoder encoder = graphProvider.getEncoder();

        Map<FieldsComparableCycle, ProfitComparableCycle> cycles = processor.findAllCycles(graph, encoder);
        eventPublisher.publishEvent(new ArbitrageurFinishEvent(this, cycles));
    }
}
