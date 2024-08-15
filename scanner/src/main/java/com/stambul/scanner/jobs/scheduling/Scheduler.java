package com.stambul.scanner.jobs.scheduling;

import com.stambul.scanner.jobs.entities.interfaces.Job;
import com.stambul.scanner.jobs.events.JobFinishEvent;
import com.stambul.scanner.jobs.events.RebootEvent;
import com.stambul.library.database.interaction.services.ConnectionManager;
import com.stambul.scanner.jobs.scheduling.interfaces.SchedulerInterface;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Scheduler implements SchedulerInterface {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final Object mutex = new Object();
    private final ThreadPoolTaskScheduler scheduler;
    private final List<Job> waitingJobs;
    private final Map<String, Job> activeJobs = new HashMap<>();
    private final ConnectionManager connectionManager;
    private final String repeatDelayISO;

    @Autowired
    public Scheduler(
            ConnectionManager connectionManager,
            List<Job> waitingJobs,
            @Qualifier("parsersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${parsers.scheduler.repeat.delay.iso}") String repeatDelayISO
    ) {
        this.connectionManager = connectionManager;
        this.waitingJobs = waitingJobs;
        this.scheduler = scheduler;
        this.repeatDelayISO = repeatDelayISO;
    }

    @PostConstruct
    private void init() {
        scheduler.schedule(this, TimeTools.toInstant("PT1S"));
    }

    @Override
    public void run() {

        try {
            connectionManager.testConnection();
            synchronized (mutex) {
                for (Job job : waitingJobs) {
                    job.launch();
                    activeJobs.put(job.getClass().getName(), job);
                }
            }
        } catch (SQLException e) {
            repeat("Database connection problem", e);
        } catch (Exception e) {
            logger.error("Could not launch jobs", e);
        }
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void handleFinishEvent(JobFinishEvent event) {
        try {

            Class<?> finishedJob = event.getJob();
            logger.debug(String.format("FINISH(%s)", finishedJob.getSimpleName()));

            synchronized (mutex) {
                Job job = activeJobs.get(finishedJob.getName());
                if (job == null) {
                    logger.error("No active job found for: " + event);
                    return;
                }
                job.finish();
            }

        } catch (Exception e) {
            logger.error("Could not handle finish event: " + event, e);
        }
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void handleRebootEvent(RebootEvent event) {
        try {

            Class<?> rebootJob = event.getJob();
            logger.error(String.format("REBOOT(%s)", rebootJob.getSimpleName()), event.getInterruptingException());

            synchronized (mutex) {
                Job job = activeJobs.get(rebootJob.getName());
                if (job == null) {
                    logger.error("No active job found for: " + event);
                    return;
                }
                job.repeat();
            }

        } catch (Exception e) {
            logger.error("Could not handle reboot event: " + event, e);
        }
    }

    private void repeat(String message, Exception cause) {
        String format = String.format("REPEAT(%s, %s): %s", this.getClass().getSimpleName(), repeatDelayISO, message);
        logger.error(format, cause);
        scheduler.schedule(this, TimeTools.toInstant(repeatDelayISO));
    }
}
