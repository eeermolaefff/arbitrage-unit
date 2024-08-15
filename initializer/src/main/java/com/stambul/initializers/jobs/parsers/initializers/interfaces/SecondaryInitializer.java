package com.stambul.initializers.jobs.parsers.initializers.interfaces;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.entities.interfaces.BatchParser;
import com.stambul.library.tools.TimeTools;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class SecondaryInitializer<D extends DataObject<D>> implements ParsersInitializer {

    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final DatabaseService databaseService;
    protected final BlacklistService blacklistService;
    protected final BatchParser<CurrencyDTO, D> parser;
    protected final boolean updateFlag;
    protected final boolean addFlag;
    protected final boolean ignoreNotActiveFlag;
    protected final int batchSize;
    protected final String delayBetweenBatchesISO;
    protected final String updateDelayISO;
    protected final Class<?> job;

    public SecondaryInitializer(
            Class<?> job,
            DatabaseService databaseService,
            BlacklistService blacklistService,
            BatchParser<CurrencyDTO, D> parser,
            boolean updateFlag,
            boolean addFlag,
            boolean ignoreNotActiveFlag,
            int batchSize,
            String delayBetweenBatchesISO,
            String updateDelayISO
    ) {
        this.job = job;
        this.blacklistService = blacklistService;
        this.databaseService = databaseService;
        this.parser = parser;
        this.updateFlag = updateFlag;
        this.addFlag = addFlag;
        this.ignoreNotActiveFlag = ignoreNotActiveFlag;
        this.batchSize = batchSize;
        this.delayBetweenBatchesISO = delayBetweenBatchesISO;
        this.updateDelayISO = updateDelayISO;
    }

    protected abstract TimestampDTO getTimestamp(CurrencyDTO currency);

    @Override
    public int initialize() {
        logger.debug(String.format("INITIALIZE(%s)", job.getSimpleName()));
        List<CurrencyDTO> allCurrencies = databaseService.getAllCurrencies();

        List<CurrencyDTO> blockedCurrencies = new LinkedList<>();
        List<CurrencyDTO> currenciesToAddActive = new LinkedList<>();
        List<CurrencyDTO> currenciesToAddNotActive = new LinkedList<>();
        List<CurrencyDTO> currenciesToUpdateActive = new ArrayList<>();
        List<CurrencyDTO> currenciesToUpdateNotActive = new ArrayList<>();
        List<CurrencyDTO> currenciesToIgnore = new LinkedList<>();

        splitCurrenciesList(
                allCurrencies, blockedCurrencies, currenciesToAddActive, currenciesToAddNotActive,
                currenciesToUpdateActive, currenciesToUpdateNotActive, currenciesToIgnore
        );

        currenciesToUpdateActive.sort((c1, c2) -> getTimestamp(c1).compareTo(getTimestamp(c2)));
        currenciesToUpdateNotActive.sort((c1, c2) -> getTimestamp(c1).compareTo(getTimestamp(c2)));

        int numberOfTasks = 0;

        numberOfTasks += tasksToIgnoreBlocked(blockedCurrencies);
        numberOfTasks += tasksToAdd(currenciesToAddActive, true);
        numberOfTasks += tasksToAdd(currenciesToAddNotActive, false);
        numberOfTasks += tasksToIgnore(currenciesToIgnore);
        numberOfTasks += tasksToUpdate(currenciesToUpdateActive, true);
        numberOfTasks += tasksToUpdate(currenciesToUpdateNotActive, false);

        return numberOfTasks;
    }

    private void splitCurrenciesList(
            List<CurrencyDTO> allCurrencies,
            List<CurrencyDTO> blockedCurrencies,
            List<CurrencyDTO> currenciesToAddActive,
            List<CurrencyDTO> currenciesToAddNotActive,
            List<CurrencyDTO> currenciesToUpdateActive,
            List<CurrencyDTO> currenciesToUpdateNotActive,
            List<CurrencyDTO> currenciesToIgnore
    ) {
        Map<String, Object> blocked = blacklistService.getBlacklist(job.getName(), "primary");

        for (CurrencyDTO currency : allCurrencies) {
            TimestampDTO timestamp = getTimestamp(currency);

            if (blocked.containsKey(currency.getId().toString())) {
                blockedCurrencies.add(currency);
            } else if ("fiat".equals(currency.getCategory())) {
                currenciesToIgnore.add(currency);
            } else if (timestamp == null) {
                if (currency.getIsActive()) currenciesToAddActive.add(currency);
                else                        currenciesToAddNotActive.add(currency);
            } else if (updateCondition(timestamp.getUpdatedAt())) {
                if (currency.getIsActive()) currenciesToUpdateActive.add(currency);
                else                        currenciesToUpdateNotActive.add(currency);
            } else {
                currenciesToIgnore.add(currency);
            }
        }
    }

    private int tasksToIgnoreBlocked(List<CurrencyDTO> blockedCurrencies) {
        if (blockedCurrencies.isEmpty())
            return 0;
        String message = "IGNORE(%s): parsing %d blocked currencies [all]";
        logger.debug(String.format(message, job.getSimpleName(), blockedCurrencies.size()));
        return 0;
    }

    private int tasksToIgnore(List<CurrencyDTO> currenciesToIgnore) {
        if (currenciesToIgnore.isEmpty())
            return 0;
        String message = "IGNORE(%s): parsing %d currencies [all]";
        logger.debug(String.format(message, job.getSimpleName(), currenciesToIgnore.size()));
        return 0;
    }

    private int tasksToAdd(List<CurrencyDTO> currenciesToAdd, boolean active) {
        if (currenciesToAdd.isEmpty())
            return 0;
        String status = active ? "active" : "not active";
        if (!addFlag) {
            String message = "IGNORE(%s): parsing %d currencies [%s] (addFlag=false)";
            logger.debug(String.format(message, job.getSimpleName(), currenciesToAdd.size(), status));
            return 0;
        }
        if (!active && ignoreNotActiveFlag) {
            String message = "IGNORE(%s): parsing %d currencies [%s] (ignoreNotActiveFlag=true)";
            logger.debug(String.format(message, job.getSimpleName(), currenciesToAdd.size(), status));
            return 0;
        }
        String message = "PARSE(%s): parsing %d currencies [%s]";
        logger.debug(String.format(message, job.getSimpleName(), currenciesToAdd.size(), status));

        return assignTasks(currenciesToAdd);
    }

    private int tasksToUpdate(List<CurrencyDTO> currenciesToUpdate, boolean active) {
        if (currenciesToUpdate.isEmpty())
            return 0;
        String status = active ? "active" : "not active";
        if (!updateFlag) {
            String message = "IGNORE(%s): updating %d currencies [%s] (updateFlag=false)";
            logger.debug(String.format(message, job.getSimpleName(), currenciesToUpdate.size(), status));
            return 0;
        }
        if (!active && ignoreNotActiveFlag) {
            String message = "IGNORE(%s): updating for %d currencies [%s] (ignoreNotActiveFlag=true)";
            logger.debug(String.format(message, job.getSimpleName(), currenciesToUpdate.size(), status));
            return 0;
        }
        String message = "PARSE(%s): updating %d currencies [%s]";
        logger.debug(String.format(message, job.getSimpleName(), currenciesToUpdate.size(), status));

        return assignTasks(currenciesToUpdate);
    }

    private int assignTasks(List<CurrencyDTO> currencies) {
        long delayBetweenBatches = TimeTools.toMilliseconds(delayBetweenBatchesISO);

        int numberOfTasks = 0;
        int size = currencies.size();
        for (int left = 0; left < size; left+= batchSize) {
            int right = Math.min(left + batchSize, size);
            List<CurrencyDTO> batch = currencies.subList(left, right);
            parser.parse(batch, delayBetweenBatches);
            numberOfTasks++;
        }

        return numberOfTasks;
    }

    private boolean updateCondition(Timestamp timestamp) {
        LocalDateTime lastUpdated = timestamp.toLocalDateTime();
        LocalDateTime currentTime = LocalDateTime.now();
        return ChronoUnit.SECONDS.between(lastUpdated, currentTime) >= TimeTools.toSeconds(updateDelayISO);
    }
}

