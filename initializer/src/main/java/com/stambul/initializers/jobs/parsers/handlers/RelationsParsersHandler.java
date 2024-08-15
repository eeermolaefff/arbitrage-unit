package com.stambul.initializers.jobs.parsers.handlers;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.initializers.jobs.entities.RelationsJob;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.initializers.RelationsParsersInitializer;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParentParsersHandler;
import com.stambul.initializers.jobs.parsers.results.RelationsParsersResults;
import com.stambul.library.tools.IdentifiablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RelationsParsersHandler extends ParentParsersHandler<RelationsParsersHandler, RelationsParsersResults> {
    private final DatabaseService databaseService;

    @Autowired
    public RelationsParsersHandler(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            ApplicationEventPublisher publisher,
            RelationsParsersInitializer initializer,
            @Qualifier("relationsParsersExecutor") ThreadPoolTaskExecutor executor,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(RelationsJob.class, publisher, executor, initializer, blacklistService, maxMessageLength);
        this.databaseService = databaseService;
    }

    @Override
    protected void insert(RelationsParsersResults result) {
        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();
        Map<Integer, Set<RelationDTO>> relationsMap = result.getSecondaryMap();

        databaseService.insertRelations(currenciesMap, relationsMap);
    }

    @Override
    protected RelationsParsersResults[] split(RelationsParsersResults result) {
        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();
        Map<Integer, Set<RelationDTO>> relationsMap = result.getSecondaryMap();

        int totalSize = 0;
        for (int currencyId : currenciesMap.keySet())
            totalSize += relationsMap.get(currencyId).size();

        if (totalSize <= 1)
            return new RelationsParsersResults[] { result };

        RelationsParsersResults[] results = new RelationsParsersResults[] {
                new RelationsParsersResults(),  new RelationsParsersResults()
        };

        int splitSize = totalSize / 2;
        int activeIdx = 0;
        int counter = 0;

        for (int currencyId : currenciesMap.keySet()) {
            CurrencyDTO currency = currenciesMap.get(currencyId);
            Set<RelationDTO> relations = relationsMap.get(currencyId);

            if (counter + relations.size() < splitSize) {
                results[activeIdx].add(currency, relations);
                counter += relations.size();
            } else {
                for (RelationDTO relation : relations) {
                    results[activeIdx].add(currency, relation);
                    if (++counter == splitSize)
                        activeIdx++;
                }
            }
        }

        return results;
    };
}
