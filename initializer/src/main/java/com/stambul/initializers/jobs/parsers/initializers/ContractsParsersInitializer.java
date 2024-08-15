package com.stambul.initializers.jobs.parsers.initializers;

import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.initializers.jobs.entities.ContractsJob;
import com.stambul.initializers.services.BlacklistService;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.initializers.jobs.parsers.entities.interfaces.BatchParser;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.SecondaryInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContractsParsersInitializer extends SecondaryInitializer<ContractDTO> {

    @Autowired
    public ContractsParsersInitializer(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            BatchParser<CurrencyDTO, ContractDTO> parser,
            @Value("${coinmarketcap.initialization.contracts.update}") boolean updateFlag,
            @Value("${coinmarketcap.initialization.contracts.add}") boolean addFlag,
            @Value("${coinmarketcap.initialization.contracts.ignore.not.active}") boolean ignoreNotActiveFlag,
            @Value("${coinmarketcap.initialization.contracts.batch.size}") int batchSize,
            @Value("${coinmarketcap.initialization.contracts.delay.between.batches.iso}") String delayBetweenBatchesISO,
            @Value("${coinmarketcap.initialization.contracts.update.delay.iso}") String updateDelayISO
    ) {
        super(
                ContractsJob.class, databaseService, blacklistService, parser, updateFlag, addFlag,
                ignoreNotActiveFlag, batchSize, delayBetweenBatchesISO, updateDelayISO
        );
    }

    @Override
    protected TimestampDTO getTimestamp(CurrencyDTO currency) {
        return currency.getContractsUpdatedAt();
    }
}
