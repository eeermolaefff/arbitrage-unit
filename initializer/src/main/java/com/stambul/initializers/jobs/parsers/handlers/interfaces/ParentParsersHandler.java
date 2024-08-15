package com.stambul.initializers.jobs.parsers.handlers.interfaces;

import com.stambul.initializers.jobs.events.JobRebootEvent;
import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.events.ParserHandlerFinishEvent;
import com.stambul.initializers.jobs.parsers.results.ContractsParsersResults;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.ParsersInitializer;
import com.stambul.initializers.jobs.parsers.results.interfaces.ParsingResult;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.library.tools.Pair;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class ParentParsersHandler<H extends ParsersHandler<H, P>, P extends ParsingResult> implements ParsersHandler<H, P> {
    protected final Class<?> job;
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final ThreadPoolTaskExecutor executor;
    protected final ParsersInitializer initializer;
    protected final Object notCompletedTasksNumberMutex = new Object();
    protected final ApplicationEventPublisher publisher;
    protected final BlacklistService blacklistService;
    protected final int maxMessageLength;
    protected int notCompletedTasksNumber, totalNumberOfTasks;

    public ParentParsersHandler(
            Class<?> job,
            ApplicationEventPublisher publisher,
            ThreadPoolTaskExecutor executor,
            ParsersInitializer initializer,
            BlacklistService blacklistService,
            int maxMessageLength
    ) {
        this.job = job;
        this.maxMessageLength = maxMessageLength;
        this.executor = executor;
        this.initializer = initializer;
        this.publisher = publisher;
        this.blacklistService = blacklistService;
    }


    protected abstract void insert(P results);
    protected abstract P[] split(P results);
    protected P filterResults(P results) { return results; };


    @Override
    public int launchParsers() {
        synchronized (notCompletedTasksNumberMutex) {
            totalNumberOfTasks = initializer.initialize();
            notCompletedTasksNumber = totalNumberOfTasks;
            return notCompletedTasksNumber;
        }
    }

    @Override
    public void reboot() {
        executor.shutdown();
        executor.initialize();
    }

    @Override
    @EventListener
    @Async("listenersExecutor")
    public void handleParsingResults(ParserFinishEvent<H, P> parserFinishEvent) {
        if (!parserFinishEvent.recipientMatch(this.getClass()))
            return;

        P results = parserFinishEvent.getParsingResults();
        logger.debug(String.format("HANDLE(%s): %s", job.getSimpleName(), results));

        ApplicationEvent newEvent = handleResults(results);
        if (newEvent == null)
            newEvent = handleProgress(results);
        if (newEvent != null)
            publisher.publishEvent(newEvent);
    }

    protected ApplicationEvent handleResults(P results) {
        ApplicationEvent event = null;

        try {
            addToDatabase(results);
            addToBlacklist(results.getBlockingResults(), "primary");
        } catch (Exception cause) {
            String message = "Could not handle parser results: results=" + results;
            event = new JobRebootEvent<>(this, job, message, cause);
        }

        return event;
    }

    protected void addToDatabase(P results) {
        try {
            String message = String.format("%s(%s): %s", "INSERT", job.getSimpleName(), results);
            logger.debug(cut(message));

            if (filterResults(results).isParsingResultEmpty()) {
                message = String.format("%s(%s): %s", "IGNORE EMPTY", job.getSimpleName(), results);
                logger.debug(cut(message));
                return;
            }

            handleInsertTask(results);
        } catch (TransactionException | PessimisticLockingFailureException e) {
            throw e;
        } catch (Exception e) {
            addToBlacklist(results.blockAllResults(e), "primary");
        }
    };

    private void handleInsertTask(P results) {
        try {
            insert(results);
        } catch (DataIntegrityViolationException e) {
            P[] split = split(results);

            if (split.length == 1) {
                addToBlacklist(split[0].blockAllResults(e), "secondary");
                return;
            }

            for (P result : split)
                handleInsertTask(result);
        }
    }

    protected void addToBlacklist(List<Pair<Identifiable, Exception>> blocks, String section) {
        if (blocks == null || blocks.isEmpty())
            return;

        List<Map<String, Object>> blocksAsJson= new LinkedList<>();

        for (Pair<Identifiable, Exception> block : blocks) {
            Identifiable blockedObject = block.getFirst();
            Exception cause = block.getSecond();

            String message = String.format("%s(%s): %s", "BLOCK", job.getSimpleName(), blockedObject);
            logger.error(cut(message), cause);
            blocksAsJson.add(getBlockedObjectAsJson(blockedObject, cause));
        }

        blacklistService.addObjectsToBlacklist(job.getName(), blocksAsJson, section);
    }

    protected Map<String, Object> getBlockedObjectAsJson(Identifiable blockedObject, Exception cause) {
        Map<String, Object> block = new TreeMap<>();

        block.put("id", blockedObject.getId());
        block.put("cause", cause.toString());
        block.put("object", blockedObject.toString());
        block.put("timestamp", TimeTools.currentTimestamp());

        return block;
    }

    protected ApplicationEvent handleProgress(P results) {
        ApplicationEvent event = null;

        try {
            synchronized (notCompletedTasksNumberMutex) {
                notCompletedTasksNumber -= results.getExecutedTaskSize();

                String format = "PROGRESS(%s): %d/%d tasks done";
                int tasksDone = totalNumberOfTasks - notCompletedTasksNumber;
                logger.info(String.format(format, job.getSimpleName(), tasksDone, totalNumberOfTasks));

                Exception interruptedException = results.getInterruptedException();
                if (interruptedException != null) {
                    String message = "The exception was thrown while parsing";
                    event = new JobRebootEvent<>(this, job, message, interruptedException);
                } else if (notCompletedTasksNumber == 0) {
                    event = new ParserHandlerFinishEvent<>(this, job);
                } else if (notCompletedTasksNumber < 0) {
                    String message = "Negative not completed tasks number value [notCompletedTasksNumber=%d]";
                    throw new RuntimeException(String.format(message, notCompletedTasksNumber));
                }

            }
        } catch (Exception cause) {
            String message = "Could not handle parser progress: results=" + results;
            event = new JobRebootEvent<>(this, job, message, cause);
        }

        return event;
    }

    protected String cut(String message) {
        if (message.length() > maxMessageLength)
            message = message.substring(0, maxMessageLength) + " [...] ";
        return message;
    }
}
