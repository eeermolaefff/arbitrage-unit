package com.stambul.initializers.jobs.entities.interfaces;

import com.stambul.initializers.jobs.events.JobFinishEvent;
import com.stambul.initializers.jobs.events.JobLaunchEvent;
import com.stambul.initializers.jobs.events.JobRebootEvent;
import com.stambul.initializers.jobs.events.ParserHandlerFinishEvent;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParsersHandler;
import com.stambul.initializers.jobs.parsers.results.interfaces.ParsingResult;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

public abstract class ParentJob<J extends ParentJob<J, H, P>, H extends ParsersHandler<H, P>, P extends ParsingResult> implements Job<J> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final ParsersHandler<H, P> parsersHandler;
    private final LaunchRequirements launchRequirements;
    private final ApplicationEventPublisher publisher;
    private final ThreadPoolTaskScheduler scheduler;
    private final String startDelayISO;
    private final String scheduleDelayISO;
    private final String repeatDelayISO;
    private final boolean launchFlag;
    private Boolean wasRebooted = false;
    private final int maxMessageLength;

    public ParentJob(
            ParsersHandler<H, P> parsersHandler,
            ApplicationEventPublisher publisher,
            ThreadPoolTaskScheduler scheduler,
            String startDelayISO,
            String scheduleDelayISO,
            String repeatDelayISO,
            boolean launchFlag,
            int maxMessageLength
    ) {
        this.parsersHandler = parsersHandler;
        this.publisher = publisher;
        this.scheduler = scheduler;
        this.startDelayISO = startDelayISO;
        this.scheduleDelayISO = scheduleDelayISO;
        this.repeatDelayISO = repeatDelayISO;
        this.launchFlag = launchFlag;
        this.maxMessageLength = maxMessageLength;
        this.launchRequirements = new LaunchRequirements(
                initRequirementsList(),
                new JobLaunchEvent<>(this, this.getClass())
        );
    }

    protected abstract List<Class> initRequirementsList();

    @Override
    public LaunchRequirements getLaunchRequirements() {
        return launchRequirements;
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void launchOnEvent(JobLaunchEvent<J> event) {
        if (!event.recipientMatch(this.getClass()))
            return;

        if (!launchFlag) {
            String message = String.format("IGNORE(%s): launchFlag=false", this.getClass().getSimpleName());
            logger.debug(cut(message));
            publisher.publishEvent(new JobFinishEvent(this));
            return;
        }

        logger.debug(String.format("SCHEDULE(%s, %s)", this.getClass().getSimpleName(), startDelayISO));
        scheduler.schedule(this, TimeTools.toInstant(startDelayISO));
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void rebootOnEvent(JobRebootEvent<J> event) {
        if (!event.recipientMatch(this.getClass()))
            return;

        repeat(event.getMessage(), event.getCause());
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void finishOnEvent(ParserHandlerFinishEvent<J> event) {
        if (!event.recipientMatch(this.getClass()))
            return;

        scheduler.schedule(this, TimeTools.toInstant(scheduleDelayISO));
        publisher.publishEvent(new JobFinishEvent(this));
    }

    @Override
    public void run() {
        synchronized (wasRebooted) {
            wasRebooted = false;
        }

        logger.debug(String.format("START(%s)", this.getClass().getSimpleName()));

        try {
            if (parsersHandler.launchParsers() == 0) {
                publisher.publishEvent(new JobFinishEvent(this));
                scheduler.schedule(this, TimeTools.toInstant(scheduleDelayISO));
            }
        } catch (Exception cause) {
            repeat("Could not initialize parsers", cause);
        }
    }

    private void repeat(String message, Exception cause) {
        synchronized (wasRebooted) {
            if (wasRebooted)
                return;

            String format = String.format("REPEAT(%s, %s): %s", this.getClass().getSimpleName(), repeatDelayISO, message);
            logger.error(format, cause);
            parsersHandler.reboot();
            scheduler.schedule(this, TimeTools.toInstant(repeatDelayISO));
            wasRebooted = true;
        }
    }

    private String cut(String message) {
        if (message.length() > maxMessageLength)
            message = message.substring(0, maxMessageLength) + " [...] ";
        return message;
    }
}
