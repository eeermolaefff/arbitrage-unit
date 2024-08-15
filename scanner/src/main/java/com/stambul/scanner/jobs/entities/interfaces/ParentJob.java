package com.stambul.scanner.jobs.entities.interfaces;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.parsers.entities.interfaces.Parser;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledFuture;

public abstract class ParentJob<J extends Job, D extends DTO<D>> implements Job {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final ThreadPoolTaskScheduler scheduler;
    private ScheduledFuture<?> parserTask;
    protected final Parser<J, D> parser;
    private final boolean launchFlag;
    private final String startDelayISO;
    private final String scheduleDelayISO;
    private final String updateDelayISO;
    private final String repeatDelayISO;
    private long counter = 0;

    public ParentJob(
            Parser<J, D> parser,
            ThreadPoolTaskScheduler scheduler,
            boolean launchFlag,
            String startDelayISO,
            String scheduleDelayISO,
            String updateDelayISO,
            String repeatDelayISO
    ) {
        this.parser = parser;
        this.scheduler = scheduler;
        this.launchFlag = launchFlag;
        this.startDelayISO = startDelayISO;
        this.scheduleDelayISO = scheduleDelayISO;
        this.updateDelayISO = updateDelayISO;
        this.repeatDelayISO = repeatDelayISO;
    }

    @Override
    public synchronized void launch() {
        if (!launchFlag) {
            logger.debug(String.format("IGNORE(%s): launchFlag=false", getClass().getSimpleName()));
            return;
        }

        String message = "SCHEDULE(%s): startDelayISO=%s, updateDelayISO=%s";
        logger.info(String.format(message, getClass().getSimpleName(), startDelayISO, updateDelayISO));

        parserTask = scheduler.schedule(parser, TimeTools.toInstant(startDelayISO));
    }

    @Override
    public synchronized void repeat() {
        String message = "REPEAT(%s): repeatDelayISO=%s";
        logger.info(String.format(message, this.getClass().getSimpleName(), repeatDelayISO));

        counter = 0;
        parserTask.cancel(true);
        parserTask = scheduler.schedule(parser, TimeTools.toInstant(repeatDelayISO));
    }

    @Override
    public synchronized void finish() {
        if (counter++ == Long.MAX_VALUE)
            counter = 0;
        logger.info(String.format("FINISH(%s): %d", getClass().getSimpleName(), counter));

        parserTask.cancel(true);
        parserTask = scheduler.schedule(parser, TimeTools.toInstant(scheduleDelayISO));
    }
}

