package com.stambul.initializers.jobs.parsers.entities;

import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.handlers.TransfersParsersHandler;
import com.stambul.initializers.jobs.parsers.results.TransferParsersResults;
import com.stambul.initializers.services.ConfigService;
import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.*;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.StringST;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransfersParser implements SimpleParser<TransferDTO> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final IOService ioService;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;
    private final DatabaseService databaseService;

    @Autowired
    public TransfersParser(
            IOService ioService,
            ConfigService configService,
            ApplicationEventPublisher eventPublisher,
            DatabaseService databaseService
    ) {
        this.ioService = ioService;
        this.configService = configService;
        this.eventPublisher = eventPublisher;
        this.databaseService = databaseService;
    }

    //TODO parse chains
    @Async("transfersParsersExecutor")
    public void parse() {
        logger.debug(String.format("PARSING(%s)", this.getClass().getSimpleName()));

        TransferParsersResults parsingResults = new TransferParsersResults();
        try {
            StringST<RelationDTO> tickerRelationMap = new StringST<>();
            StringST<BlockchainDTO> blockchainsMap = new StringST<>();

            initMaps(tickerRelationMap, blockchainsMap);
            parse(parsingResults, tickerRelationMap, blockchainsMap);

            parsingResults.taskDone();
        } catch (Exception interruptedException) {
            parsingResults.setInterruptedException(interruptedException);
        }

        eventPublisher.publishEvent(new ParserFinishEvent<>(this, TransfersParsersHandler.class, parsingResults));
    }

    private void parse(
            TransferParsersResults parsingResults,
            StringST<RelationDTO> relationsMap,
            StringST<BlockchainDTO> blockchainsMap
    ) {
//        for (String ticker : relationsMap.keys()) {
//            logger.info(ticker + " -> " + relationsMap.get(ticker));
//        }
//        for (String ticker : blockchainsMap.keys()) {
//            logger.info(ticker + " -> " + blockchainsMap.get(ticker));
//        }

        for (String currencyTicker : relationsMap.keys()) {
            RelationDTO relation = relationsMap.get(currencyTicker);
            for (String blockchainTicker : blockchainsMap.keys()) {
                BlockchainDTO blockchain = blockchainsMap.get(blockchainTicker);
                TransferDTO transfer = new TransferDTO(
                        relation, blockchain, true, true,
                        1., 1.,
                        1., 1.
                );
                parsingResults.add(transfer);
            }
        }
    }

    private void initMaps(
            StringST<RelationDTO> tickerRelationMap,
            StringST<BlockchainDTO> blockchainsMap
    ) {
        //TODO make normal market id initialization
        Map<String, Object> config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());


        for (RelationDTO relation : databaseService.getRelationsByMarketsIds((int) config.get("marketId")))
            tickerRelationMap.put(relation.getTicker().getTicker(), relation);


        Map<String, Object> configBlockchainsMap = (Map<String, Object>) config.get("blockchainsMap");

        Map<Integer, String> idTickerMap = new TreeMap<>();
        for (String ticker : configBlockchainsMap.keySet()) {
            int id = (int) configBlockchainsMap.get(ticker);
            idTickerMap.put(id, ticker);
        }

        for (var entry : databaseService.getBlockchainsMap(idTickerMap.keySet()).entrySet()) {
            String ticker = idTickerMap.get(entry.getKey());
            if (ticker == null)
                throw new RuntimeException("Ticker not found for blockchain: " + entry);
            blockchainsMap.put(ticker, entry.getValue());
        }
    }
}
