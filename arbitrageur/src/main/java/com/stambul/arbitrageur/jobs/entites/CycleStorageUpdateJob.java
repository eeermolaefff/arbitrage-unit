package com.stambul.arbitrageur.jobs.entites;

import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.processing.CycleStorage;
import com.stambul.arbitrageur.jobs.entites.interfaces.FinishEventHandler;
import com.stambul.arbitrageur.jobs.entites.interfaces.Job;
import com.stambul.arbitrageur.jobs.events.ArbitrageurFinishEvent;
import com.stambul.library.database.interaction.services.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CycleStorageUpdateJob extends Job implements FinishEventHandler {
    private final CycleStorage cycleStorage;

    @Autowired
    public CycleStorageUpdateJob(
            CycleStorage cycleStorage,
            ConnectionManager connectionManager,
            @Qualifier("arbitrageScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${updater.repeat.delay.iso}") String repeatDelayISO,
            @Value("${updater.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${updater.start.delay.iso}") String startDelayISO,
            @Value("${updater.launch}") boolean launchFlag,
            @Value("${updater.console.log}") boolean logFlag
    ) {
        super(connectionManager, scheduler, repeatDelayISO, scheduleDelayISO, startDelayISO, launchFlag, logFlag);
        this.cycleStorage = cycleStorage;
    }

    @Override
    protected void bodyActions() {
        cycleStorage.updateAll();
    }

    @EventListener
    @Async("arbitrageExecutor")
    public void handleFinishEvent(ArbitrageurFinishEvent finishEvent) {
        Map<FieldsComparableCycle, ProfitComparableCycle> cycles = finishEvent.getFoundCycles();

        String identifier = finishEvent.getSource().getClass().getSimpleName();
        logger.debug(String.format("HANDLE(%s): %s", identifier, finishEvent));

        long t1 = System.currentTimeMillis();
        cycleStorage.addAll(cycles);
        long t2 = System.currentTimeMillis();
        double delay = (t2 - t1) / 1000.;

        logger.debug(String.format("HANDLED(%s): %.3f seconds", identifier, delay));
    }
}
