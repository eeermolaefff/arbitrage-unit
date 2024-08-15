package com.stambul.scanner.jobs.parsers.entities.spot;


import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.tools.Pair;
import com.stambul.scanner.jobs.entities.spot.BinanceJob;
import com.stambul.scanner.jobs.parsers.entities.interfaces.ParentParser;
import com.stambul.scanner.jobs.parsers.entities.spot.interfaces.SpotParentParser;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.library.tools.IOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.stambul.scanner.services.DatabaseService;

import java.util.*;

@Service
public class BinanceParser extends SpotParentParser<BinanceJob, OrderbookDTO> {

    @Autowired
    public BinanceParser(
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            @Value("${binance.spot.update.delay.iso}") String updateDelayISO
    ) {
        super(BinanceJob.class, OrderbookDTO.class, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }

    @Override
    protected void updateExchangeInfoMap() {
        String exchangeInfoURL = (String) config.get("exchangeInfoURL");
        exchangeInfo = new TreeMap<>();
        Map<String, Object> pairs = (Map<String, Object>) ioService.parseInfoAsJSON(exchangeInfoURL);
        for (Object pair : (List<Object>) pairs.get("symbols")) {
            Map<String, Object> pairInfo = (Map<String, Object>) pair;
            exchangeInfo.put((String) pairInfo.get("symbol"), pairInfo);
        }
    }

    @Override
    protected ParsingResults<OrderbookDTO> parse() {
        List<OrderbookDTO> orderbooks = new LinkedList<>();
        List<Pair<String, Exception>> exceptions = new LinkedList<>();

        Map<String, Object> tickerInfo = getTickerInfoMap();
        ParsingResults<OrderbookDTO> parsingResults = new ParsingResults<>(
                OrderbookDTO.class, getClass().getSimpleName()
        );

        for (String pairTicker : exchangeInfo.keySet()) {
            Map<String, Object> pairExchangeInfo = (Map<String, Object>) exchangeInfo.get(pairTicker);
            if (!pairExchangeInfo.get("status").equals("TRADING"))
                continue;
            Map<String, Object> pairTickerInfo = (Map<String, Object>) tickerInfo.get(pairTicker);

            try {
                OrderbookDTO orderbook = jsonToOrderbook(pairTicker, pairExchangeInfo, pairTickerInfo);
                orderbooks.add(orderbook);
            } catch (Exception e) {
                RuntimeException root = new RuntimeException("Can't convert json to orderbook: ticker=" + pairTicker, e);
                exceptions.add(new Pair<>(pairTicker, root));
            }
        }

        parsingResults.addAll(orderbooks, (int) config.get("marketId"));
        parsingResults.blockAll(exceptions, (int) config.get("marketId"));
        return parsingResults;
    }

    private Map<String, Object> getTickerInfoMap() {
        String priceInfoURL = (String) config.get("priceInfoURL");
        Map<String, Object> tickerInfo = new TreeMap<>();
        for (Object pair : (List<Object>) ioService.parseInfoAsJSON(priceInfoURL)) {
            Map<String, Object> pairInfo = (Map<String, Object>) pair;
            tickerInfo.put((String) pairInfo.get("symbol"), pairInfo);
        }
        return tickerInfo;
    }

    private OrderbookDTO jsonToOrderbook(
            String pairTicker,
            Map<String, Object> pairExchangeInfo,
            Map<String, Object> pairTickerInfo
    ) {
        Boolean isActive = pairExchangeInfo.get("status").equals("TRADING");
        Double bidPrice = Double.parseDouble((String) pairTickerInfo.get("bidPrice"));
        Double askPrice = Double.parseDouble((String) pairTickerInfo.get("askPrice"));
        Double bidQty = Double.parseDouble((String) pairTickerInfo.get("bidQty"));
        Double askQty = Double.parseDouble((String) pairTickerInfo.get("askQty"));

        Double dailyVolumeUsd = null;
        Double dailyVolumeBase = Double.parseDouble((String) pairTickerInfo.get("volume"));
        Double dailyVolumeQuote = Double.parseDouble((String) pairTickerInfo.get("quoteVolume"));

        Boolean isSpotTradingAllowed = (Boolean) pairExchangeInfo.get("isSpotTradingAllowed");
        Boolean isMarginTradingAllowed = (Boolean) pairExchangeInfo.get("isMarginTradingAllowed");
        String tradingType = "cex";

        String baseAsset = (String) pairExchangeInfo.get("baseAsset");
        String quoteAsset = (String) pairExchangeInfo.get("quoteAsset");
        TradingPairDTO tradingPair = getTradingPair(pairTicker, baseAsset, quoteAsset);

        return new OrderbookDTO(
                tradingPair, isActive, bidPrice, askPrice, bidQty, askQty, dailyVolumeUsd,
                dailyVolumeBase, dailyVolumeQuote, isSpotTradingAllowed, isMarginTradingAllowed, tradingType
        );
    }
}
