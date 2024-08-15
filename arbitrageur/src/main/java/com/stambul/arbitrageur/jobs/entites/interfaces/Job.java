package com.stambul.arbitrageur.jobs.entites.interfaces;

import com.stambul.library.database.interaction.services.ConnectionManager;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

public abstract class Job implements Runnable {
    private interface LogFunction { void log(Object o); }
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final ThreadPoolTaskScheduler scheduler;
    protected final ConnectionManager connectionManager;
    protected final String repeatDelayISO, scheduleDelayISO, startDelayISO;
    protected final boolean launchFlag, logFlag;
    protected ScheduledFuture<?> task;

    public Job(
            ConnectionManager connectionManager,
            ThreadPoolTaskScheduler scheduler,
            String repeatDelayISO,
            String scheduleDelayISO,
            String startDelayISO,
            boolean launchFlag,
            boolean logFlag
    ) {
        this.connectionManager = connectionManager;
        this.scheduler = scheduler;
        this.repeatDelayISO = repeatDelayISO;
        this.scheduleDelayISO = scheduleDelayISO;
        this.startDelayISO = startDelayISO;
        this.launchFlag = launchFlag;
        this.logFlag = logFlag;
    }

    protected abstract void bodyActions();

    @Override
    public void run() {
        try {
            execute(logFlag ? logger::info : logger::debug);
        } catch (SQLException e) {
            repeat("Database connection problem", e);
        } catch (Exception e) {
            repeat("Could not handle arbitrage", e);
        }
    }

    private void execute(LogFunction logger) throws SQLException {
        logger.log(String.format("START(%s)", this.getClass().getSimpleName()));
        connectionManager.testConnection();

        long t1 = System.currentTimeMillis();
        bodyActions();
        long t2 = System.currentTimeMillis();
        double delay = (t2 - t1) / 1000.;

        String message = String.format("FINISH(%s): %.3f seconds", this.getClass().getSimpleName(), delay);
        logger.log(message);
    }

    @PostConstruct
    protected void init() {
       launch(startDelayISO);
    }

    private void launch(String startDelayISO) {
        if (!launchFlag)
            return;
        task = scheduler.scheduleWithFixedDelay(
                this, TimeTools.toInstant(startDelayISO), TimeTools.toDuration(scheduleDelayISO)
        );
    }

    protected void repeat(String message, Exception cause) {
        String format = String.format("REPEAT(%s, %s): %s", this.getClass().getSimpleName(), repeatDelayISO, message);
        logger.error(format, cause);
        while (!task.isDone())
            task.cancel(true);
        launch(repeatDelayISO);
    }
}
