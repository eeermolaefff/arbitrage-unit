package com.stambul.initializers.jobs.parsers.handlers;

import com.stambul.initializers.jobs.entities.TransfersJob;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParentParsersHandler;
import com.stambul.initializers.jobs.parsers.initializers.TransfersParsersInitializer;
import com.stambul.initializers.jobs.parsers.results.TransferParsersResults;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.TransferDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TransfersParsersHandler extends ParentParsersHandler<TransfersParsersHandler, TransferParsersResults> {
    private final DatabaseService databaseService;

    @Autowired
    public TransfersParsersHandler(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            ApplicationEventPublisher publisher,
            TransfersParsersInitializer initializer,
            @Qualifier("transfersParsersExecutor") ThreadPoolTaskExecutor executor,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(TransfersJob.class, publisher, executor, initializer, blacklistService, maxMessageLength);
        this.databaseService = databaseService;
    }

    @Override
    protected void insert(TransferParsersResults result) {
        databaseService.insertTransfers(result.getParsingResults());
    }

    @Override
    protected TransferParsersResults[] split(TransferParsersResults result) {
        Set<TransferDTO> transfers = result.getParsingResults();

        if (transfers.size() <= 1)
            return new TransferParsersResults[] { result };

        TransferParsersResults[] results = new TransferParsersResults[] {
                new TransferParsersResults(),  new TransferParsersResults()
        };

        int splitSize = transfers.size() / 2;
        int activeIdx = 0;
        int counter = 0;

        for (TransferDTO transfer : transfers) {
            results[activeIdx].add(transfer);
            if (++counter == splitSize)
                activeIdx++;
        }

        return results;
    };
}