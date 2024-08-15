package com.stambul.arbitrageur.services;

import com.stambul.library.database.interaction.services.*;
import com.stambul.library.database.objects.dto.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class DatabaseService {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final TransferService transferService;
    private final OrderbookService orderbookService;
    private final SwapService swapService;
    private final MarketsService marketsService;
    private final CurrenciesService currenciesService;

    @Autowired
    public DatabaseService(
            CurrenciesService currenciesService,
            MarketsService marketsService,
            TransferService transferService,
            OrderbookService orderbookService,
            SwapService swapService
    ) {
        this.currenciesService = currenciesService;
        this.marketsService = marketsService;
        this.transferService = transferService;
        this.orderbookService = orderbookService;
        this.swapService = swapService;
    }

    public List<OrderbookDTO> getAllOrderbooks() {
        return orderbookService.getAll();
    }

    public List<SwapDTO> getAllSwaps() {
        return swapService.getAll();
    }

    public List<TransferDTO> getAllTransfers() {
        return transferService.getAll();
    }

    public Map<Integer, SwapDTO> getSwapMap(Iterable<Integer> ids) {
        return swapService.getMap(ids);
    }

    public Map<Integer, OrderbookDTO> getOrderbookMap(Iterable<Integer> ids) {
        return orderbookService.getMap(ids);
    }

    public Map<Integer, MarketDTO> getMarketMapById(Iterable<Integer> ids) {
        return marketsService.getMap(ids);
    }

    public Map<Integer, CurrencyDTO> getCurrencyMapById(Iterable<Integer> ids) {
        return currenciesService.getMap(ids);
    }
}