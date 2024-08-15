package com.stambul.arbitrageur.jobs.entites;

import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.processing.CycleStorage;
import com.stambul.arbitrageur.jobs.entites.interfaces.Job;
import com.stambul.library.database.interaction.services.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DashboardJob extends Job {
    private final CycleStorage cycleStorage;

    @Autowired
    public DashboardJob(
            CycleStorage cycleStorage,
            ConnectionManager connectionManager,
            @Qualifier("arbitrageScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${dashboard.repeat.delay.iso}") String repeatDelayISO,
            @Value("${dashboard.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${dashboard.start.delay.iso}") String startDelayISO,
            @Value("${dashboard.launch}") boolean launchFlag,
            @Value("${dashboard.console.log}") boolean logFlag
    ) {
        super(connectionManager, scheduler, repeatDelayISO, scheduleDelayISO, startDelayISO, launchFlag, logFlag);
        this.cycleStorage = cycleStorage;
    }

    @Override
    protected void bodyActions() {
        //TODO push to dashboard
        logger.info(cycleStorage.getAll());
    }
}
