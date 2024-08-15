package com.stambul.initializers.jobs.parsers.handlers;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.initializers.jobs.entities.CurrenciesJob;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.initializers.CurrenciesParsersInitializer;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParentParsersHandler;
import com.stambul.initializers.jobs.parsers.results.CurrenciesParsersResults;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class CurrenciesParsersHandler extends ParentParsersHandler<CurrenciesParsersHandler, CurrenciesParsersResults> {
    private final DatabaseService databaseService;

    @Autowired
    public CurrenciesParsersHandler(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            ApplicationEventPublisher publisher,
            CurrenciesParsersInitializer initializer,
            @Qualifier("currenciesParsersExecutor") ThreadPoolTaskExecutor executor,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(CurrenciesJob.class, publisher, executor, initializer, blacklistService, maxMessageLength);
        this.databaseService = databaseService;
    }

    @Override
    protected void insert(CurrenciesParsersResults result) {
        databaseService.insertCurrencies(result.getPrimaryMap());
    }

    @Override
    protected CurrenciesParsersResults[] split(CurrenciesParsersResults result) {
        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();

        if (currenciesMap.size() <= 1)
            return new CurrenciesParsersResults[] { result };


        CurrenciesParsersResults[] results = new CurrenciesParsersResults[] {
                new CurrenciesParsersResults(),  new CurrenciesParsersResults()
        };

        int splitSize = currenciesMap.size() / 2;
        int activeIdx = 0;
        int counter = 0;

        for (CurrencyDTO currency : currenciesMap.values()) {
            results[activeIdx].add(currency);
            if (++counter == splitSize)
                activeIdx++;
        }

        return results;
    };
}