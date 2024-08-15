package com.stambul.scanner.jobs.parsers.entities.interfaces;

import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.scanner.services.DatabaseService;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.entities.interfaces.Job;
import com.stambul.scanner.jobs.events.ParserFinishEvent;
import com.stambul.scanner.jobs.events.RebootEvent;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ParentParser<J extends Job, D extends DTO<D>> implements Parser<J, D> {
    protected interface FieldUpdater { void update(); }
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final IOService ioService;
    protected final DatabaseService databaseService;
    private final Iterable<FieldUpdater> updateTasks;
    private final ApplicationEventPublisher eventPublisher;
    private Long exchangeInfoUpdatedTimestamp;
    private final Long updateDelayMills;
    protected final Map<String, Object> config;
    protected final Class<J> jobClass;
    protected final Class<D> dataClass;

    public ParentParser(
            Class<J> jobClass,
            Class<D> dataClass,
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            String updateDelayISO
    ) {
        this.jobClass = jobClass;
        this.dataClass = dataClass;
        this.ioService = ioService;
        this.databaseService = databaseService;
        this.eventPublisher = eventPublisher;
        this.config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());
        this.updateDelayMills = TimeTools.toMilliseconds(updateDelayISO);
        this.updateTasks = getUpdateTasks();
    }

    protected abstract ParsingResults<D> parse();
    protected abstract Iterable<FieldUpdater> getUpdateTasks();

    @Override
    public void run() {
        logger.debug(String.format("PARSE(%s)", jobClass.getSimpleName()));

        try {
            if (updateCondition())
                updateExchangeInfo();
        } catch (Exception e) {
            RuntimeException cause = new RuntimeException("Failed to update exchange info", e);
            eventPublisher.publishEvent(new RebootEvent(this, jobClass, cause));
            return;
        }

        ApplicationEvent event;
        try {
            ParsingResults<D> results = parse();
            event = new ParserFinishEvent<>(this, jobClass, dataClass, (int) config.get("marketId"), results);
        } catch (Exception e) {
            event = new RebootEvent(this, jobClass, e);
        }
        eventPublisher.publishEvent(event);

        logger.debug(String.format("PARSED(%s)", jobClass.getSimpleName()));
    }

    private boolean updateCondition() {
        if (exchangeInfoUpdatedTimestamp == null)
            return true;
        return (System.currentTimeMillis() - exchangeInfoUpdatedTimestamp) > updateDelayMills;
    }

    @Override
    public void updateExchangeInfo() {
        logger.debug(String.format("UPDATE(%s)", jobClass.getSimpleName()));

        for (FieldUpdater updater : updateTasks)
            updater.update();
        exchangeInfoUpdatedTimestamp = System.currentTimeMillis();

        logger.info(String.format("UPDATED(%s)", jobClass.getSimpleName()));
    }
}
