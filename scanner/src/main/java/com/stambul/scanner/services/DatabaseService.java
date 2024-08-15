package com.stambul.scanner.services;

import com.stambul.library.database.interaction.services.*;
import com.stambul.library.database.objects.dto.*;
import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.events.ParserFinishEvent;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DatabaseService {
    private interface Handler<D extends DTO<D>> { void handle(Iterable<D> handledList, int marketId, String parserName);}
    private final Logger logger = Logger.getLogger(this.getClass());
    private final TradingPairService tradingPairService;
    private final RelationsService relationsService;
    private final OrderbookService orderbookService;
    private final SwapService swapService;
    private final ContractsService contractsService;

    @Autowired
    public DatabaseService(
            TradingPairService tradingPairService,
            RelationsService relationsService,
            OrderbookService orderbookService,
            SwapService swapService,
            ContractsService contractsService
    ) {
        this.tradingPairService = tradingPairService;
        this.relationsService = relationsService;
        this.orderbookService = orderbookService;
        this.swapService = swapService;
        this.contractsService = contractsService;
    }

    public Iterable<ContractDTO> getContractsByBlockchainIds(Integer id) {
        return contractsService.getByBlockchainIds(List.of(id));
    }

    public List<RelationDTO> getRelationsByMarketsIds(Integer id) {
        return relationsService.getByMarketIds(List.of(id));
    }

    @Transactional
    public void insertParsingResults(ParsingResults<?> parsingResults) {
        if (parsingResults.getDataClass() == OrderbookDTO.class) {
            insert((ParsingResults<OrderbookDTO>) parsingResults, this::insertOrderbooks);
        } else if (parsingResults.getDataClass() == SwapDTO.class) {
            insert((ParsingResults<SwapDTO>) parsingResults, this::insertSwaps);
        } else {
            throw new RuntimeException("Unknown data class for parsingResults=" + parsingResults);
        }
    }

    private <T extends DTO<T>> void insert(ParsingResults<T> parsingResults, Handler<T> handler) {
        for (int marketId : parsingResults.getMarketIds()) {
            Iterable<T> resultsList = parsingResults.getParsingResults(marketId);
            handler.handle(resultsList, marketId, parsingResults.getParserName());
        }
    }


    private void insertOrderbooks(Iterable<OrderbookDTO> orderbookList, int marketId, String parserName) {
        List<Integer> tradingPairsIds = tradingPairService.getByMarketId(marketId)
                .stream().map(TradingPairDTO::getId).collect(Collectors.toList());
        Map<OrderbookDTO, OrderbookDTO> alreadyExistedOrderbooks = orderbookService.getByTradingPairIds(tradingPairsIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        Set<OrderbookDTO> orderbooksToAdd = new TreeSet<>();
        Set<OrderbookDTO> orderbooksToUpdate = new TreeSet<>();

        for (OrderbookDTO parsedOrderbook : orderbookList) {
            OrderbookDTO existedOrderbook = alreadyExistedOrderbooks.get(parsedOrderbook);
            if (existedOrderbook == null)
                orderbooksToAdd.add(parsedOrderbook);
            else {
                existedOrderbook.updateFields(parsedOrderbook);
                orderbooksToUpdate.add(existedOrderbook);
            }
        }

        orderbookService.addToDatabase(orderbooksToAdd);
        orderbookService.updateInDatabase(orderbooksToUpdate);

        logger.debug(String.format("INSERTED(%s)", parserName));
    }

    private void insertSwaps(Iterable<SwapDTO> swapList, int marketId, String parserName) {
        List<Integer> tradingPairsIds = tradingPairService.getByMarketId(marketId)
                .stream().map(TradingPairDTO::getId).collect(Collectors.toList());

        Map<SwapDTO, SwapDTO> alreadyExistedSwaps = swapService.getByTradingPairIds(tradingPairsIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        Set<SwapDTO> swapsToAdd = new TreeSet<>();
        Set<SwapDTO> swapsToUpdate = new TreeSet<>();

        for (SwapDTO parsedSwap : swapList) {
            SwapDTO existedSwap = alreadyExistedSwaps.get(parsedSwap);
            if (existedSwap == null)
                swapsToAdd.add(parsedSwap);
            else {
                existedSwap.updateFields(parsedSwap);
                swapsToUpdate.add(existedSwap);
            }
        }

        swapService.addToDatabase(swapsToAdd);
        swapService.updateInDatabase(swapsToUpdate);

        logger.debug(String.format("INSERTED(%s)", parserName));
    }

}
