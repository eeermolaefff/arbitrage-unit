package com.stambul.initializers.jobs.parsers.initializers;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.initializers.jobs.entities.RelationsJob;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.entities.interfaces.BatchParser;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.SecondaryInitializer;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RelationsParsersInitializer extends SecondaryInitializer<RelationDTO> {

    @Autowired
    public RelationsParsersInitializer(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            BatchParser<CurrencyDTO, RelationDTO> parser,
            @Value("${coinmarketcap.initialization.relations.update}") boolean updateFlag,
            @Value("${coinmarketcap.initialization.relations.add}") boolean addFlag,
            @Value("${coinmarketcap.initialization.relations.ignore.not.active}") boolean ignoreNotActiveFlag,
            @Value("${coinmarketcap.initialization.relations.batch.size}") int batchSize,
            @Value("${coinmarketcap.initialization.relations.delay.between.batches.iso}") String delayBetweenBatchesISO,
            @Value("${coinmarketcap.initialization.relations.update.delay.iso}") String updateDelayISO
    ) {
        super(
                RelationsJob.class, databaseService, blacklistService, parser, updateFlag, addFlag,
                ignoreNotActiveFlag, batchSize, delayBetweenBatchesISO, updateDelayISO
        );
    }

    @Override
    protected TimestampDTO getTimestamp(CurrencyDTO currency) {
        return currency.getRelationsUpdatedAt();
    }
}