package com.stambul.initializers.jobs.parsers.handlers;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.initializers.jobs.entities.MarketsJob;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.initializers.MarketsParsersInitializer;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParentParsersHandler;
import com.stambul.initializers.jobs.parsers.results.MarketsParsersResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MarketsParsersHandler extends ParentParsersHandler<MarketsParsersHandler, MarketsParsersResults> {
    private final DatabaseService databaseService;

    @Autowired
    public MarketsParsersHandler(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            ApplicationEventPublisher publisher,
            MarketsParsersInitializer initializer,
            @Qualifier("marketsParsersExecutor") ThreadPoolTaskExecutor executor,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(MarketsJob.class, publisher, executor, initializer, blacklistService, maxMessageLength);
        this.databaseService = databaseService;
    }

    @Override
    protected void insert(MarketsParsersResults result) {
        databaseService.insertMarkets(result.getPrimaryMap());
    }

    @Override
    protected MarketsParsersResults[] split(MarketsParsersResults result) {
        Map<Integer, MarketDTO> marketsMap = result.getPrimaryMap();

        if (marketsMap.size() <= 1)
            return new MarketsParsersResults[] { result };

        MarketsParsersResults[] results = new MarketsParsersResults[] {
                new MarketsParsersResults(),  new MarketsParsersResults()
        };

        int splitSize = marketsMap.size() / 2;
        int activeIdx = 0;
        int counter = 0;

        for (MarketDTO market : marketsMap.values()) {
            results[activeIdx].add(market);
            if (++counter == splitSize)
                activeIdx++;
        }

        return results;
    };
}