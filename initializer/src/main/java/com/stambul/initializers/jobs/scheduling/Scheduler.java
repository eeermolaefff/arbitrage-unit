package com.stambul.initializers.jobs.scheduling;

import com.stambul.initializers.jobs.entities.interfaces.Job;
import com.stambul.initializers.jobs.entities.interfaces.LaunchRequirements;
import com.stambul.initializers.jobs.events.JobFinishEvent;
import com.stambul.initializers.jobs.events.JobLaunchEvent;
import com.stambul.library.database.interaction.services.ConnectionManager;
import com.stambul.initializers.jobs.scheduling.interfaces.SchedulerInterface;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

@Service
public class Scheduler implements SchedulerInterface {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final Object waitingJobsMutex = new Object();
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConnectionManager connectionManager;
    private final ApplicationEventPublisher eventPublisher;
    private final List<Job> waitingJobs;
    private final boolean launchFlag;
    private final String repeatDelayISO;

    @Autowired
    public Scheduler(
            ConnectionManager connectionManager,
            List<Job> waitingJobs,
            ApplicationEventPublisher eventPublisher,
            @Qualifier("initializersScheduler") ThreadPoolTaskScheduler taskScheduler,
            @Value("${coinmarketcap.initialization.scheduler.launch}") boolean launchFlag,
            @Value("${coinmarketcap.initialization.scheduler.repeat.delay.iso}") String repeatDelayISO
    ) {
        this.connectionManager = connectionManager;
        this.taskScheduler = taskScheduler;
        this.eventPublisher = eventPublisher;
        this.waitingJobs = waitingJobs;
        this.launchFlag = launchFlag;
        this.repeatDelayISO = repeatDelayISO;
    }

    @PostConstruct
    private void init() {
        if (launchFlag)
            taskScheduler.schedule(this, TimeTools.toInstant("PT1S"));
    }

    @Override
    public void run() {
        try {
            connectionManager.testConnection();
            launchSatisfiedJobs();
        } catch (SQLException e) {
            repeat("Database connection problem", e);
        } catch (Exception e) {
            repeat("Could not launch satisfied jobs", e);
        }
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void handleFinishEvent(JobFinishEvent event) {
        try {
            logger.info(String.format("FINISH(%s)", event.getSource().getClass().getSimpleName()));
            satisfyJobs(event);
            launchSatisfiedJobs();
        } catch (Exception e) {
            logger.error("Could not handle finish event: " + event, e);
        }
    }

    private void satisfyJobs(JobFinishEvent event) {
        synchronized (waitingJobsMutex) {
            if (waitingJobs.isEmpty())
                return;
            waitingJobs.forEach(job -> job.getLaunchRequirements().satisfy(event.getSource().getClass()));
        }
    }

    private void launchSatisfiedJobs() {
        synchronized (waitingJobsMutex) {
            if (waitingJobs.isEmpty())
                return;

            Iterator<Job> iterator = waitingJobs.iterator();
            while (iterator.hasNext()) {
                Job<?> job = iterator.next();
                LaunchRequirements requirements = job.getLaunchRequirements();
                if (requirements.isSatisfied()) {
                    iterator.remove();
                    publishLaunchEvent(requirements.getLaunchEvent());
                }
            }
        }
    }

    private void publishLaunchEvent(JobLaunchEvent<?> launchEvent) {
        try {
            logger.info(String.format("LAUNCH(%s):", launchEvent.getRecipient().getSimpleName()));
            eventPublisher.publishEvent(launchEvent);
        } catch (Exception e) {
            logger.error("Can not publish launch request for " + launchEvent, e);
        }
    }

    private void repeat(String message, Exception cause) {
        String format = String.format("REPEAT(%s, %s): %s", this.getClass().getSimpleName(), repeatDelayISO, message);
        logger.error(format, cause);
        taskScheduler.schedule(this, TimeTools.toInstant(repeatDelayISO));
    }
}
