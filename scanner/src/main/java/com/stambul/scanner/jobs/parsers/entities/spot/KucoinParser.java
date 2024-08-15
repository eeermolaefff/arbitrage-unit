package com.stambul.scanner.jobs.parsers.entities.spot;

import com.stambul.library.tools.Pair;
import com.stambul.scanner.jobs.parsers.entities.spot.interfaces.SpotParentParser;
import com.stambul.scanner.services.DatabaseService;
import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.scanner.jobs.entities.spot.KucoinJob;
import com.stambul.scanner.jobs.parsers.entities.interfaces.ParentParser;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.library.tools.IOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KucoinParser extends SpotParentParser<KucoinJob, OrderbookDTO> {

    private final Set<String> tickersSet = new HashSet<>();

    @Autowired
    public KucoinParser(
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            @Value("${kucoin.spot.update.delay.iso}") String updateDelayISO
    ) {
        super(KucoinJob.class, OrderbookDTO.class, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }

    @Override
    protected void updateExchangeInfoMap() {
        String exchangeInfoURL = (String) config.get("exchangeInfoURL");

        exchangeInfo = new TreeMap<>();
        for (Object pair : (List<Object>) ((Map<String, Object>) ioService.parseInfoAsJSON(exchangeInfoURL)).get("data")) {
            Map<String, Object> pairInfo = (Map<String, Object>) pair;
            exchangeInfo.put((String) pairInfo.get("name"), pairInfo);

            tickersSet.add(((String) pairInfo.get("baseCurrency")).toLowerCase());
            tickersSet.add(((String) pairInfo.get("quoteCurrency")).toLowerCase());
        }
    }


    private Map<String, Object> getTickerInfoMap() {
        String priceInfoURL = (String) config.get("priceInfoURL");
        Map<String, Object> parsedJsonInfo = (Map<String, Object>) ioService.parseInfoAsJSON(priceInfoURL);

        Map<String, Object> tickerInfo = new TreeMap<>();
        for (Object pair : (List<Object>) ioService.extractDataFromJson(parsedJsonInfo, "data.ticker")) {
            Map<String, Object> pairInfo = (Map<String, Object>) pair;
            tickerInfo.put((String) pairInfo.get("symbolName"), pairInfo);
        }
        return tickerInfo;
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
            if (containsLeverageToken(pairTicker)) {
                RuntimeException root = new RuntimeException("Contains leverage token: ticker=" + pairTicker);
                exceptions.add(new Pair<>(pairTicker, root));
                continue;
            }

            try {
                Map<String, Object> pairExchangeInfo = (Map<String, Object>) exchangeInfo.get(pairTicker);
                if (! (boolean) pairExchangeInfo.get("enableTrading"))
                    continue;

                Map<String, Object> pairTickerInfo = (Map<String, Object>) tickerInfo.get(pairTicker);
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

    private OrderbookDTO jsonToOrderbook(
            String pairTicker,
            Map<String, Object> pairExchangeInfo,
            Map<String, Object> pairTickerInfo
    ) {
        Boolean isActive = (boolean) pairExchangeInfo.get("enableTrading");
        Double bidPrice = Double.parseDouble((String) pairTickerInfo.get("buy"));
        Double askPrice = Double.parseDouble((String) pairTickerInfo.get("sell"));
        Double bidQty = Double.parseDouble((String) pairTickerInfo.get("bestBidSize"));
        Double askQty = Double.parseDouble((String) pairTickerInfo.get("bestAskSize"));

        Double dailyVolumeUsd = null;
        Double dailyVolumeBase = Double.parseDouble((String) pairTickerInfo.get("vol"));
        Double dailyVolumeQuote = Double.parseDouble((String) pairTickerInfo.get("volValue"));

        Boolean isSpotTradingAllowed = (boolean) pairExchangeInfo.get("enableTrading");
        Boolean isMarginTradingAllowed = (boolean) pairExchangeInfo.get("isMarginEnabled");
        String tradingType = "cex";

        String baseAsset = (String) pairExchangeInfo.get("baseCurrency");
        String quoteAsset = (String) pairExchangeInfo.get("quoteCurrency");
        TradingPairDTO tradingPair = getTradingPair(pairTicker, baseAsset, quoteAsset);

        return new OrderbookDTO(
                tradingPair, isActive, bidPrice, askPrice, bidQty, askQty, dailyVolumeUsd,
                dailyVolumeBase, dailyVolumeQuote, isSpotTradingAllowed, isMarginTradingAllowed, tradingType
        );
    }

    private boolean containsLeverageToken(String pairTicker) {
        String pairTickerLowerCase = pairTicker.toLowerCase();
        String[] split = pairTickerLowerCase.split("-");
        return isLeverageToken(split[0]) || isLeverageToken(split[1]);
    }

    private boolean isLeverageToken(String ticker) {
        return  checkLeverageDigitPattern(ticker)
                || checkLeveragePattern(ticker, "down")
                || checkLeveragePattern(ticker, "up");
    }

    private boolean checkLeveragePattern(String ticker, String pattern) {
        if (ticker.endsWith(pattern)) {
            String basePattern = ticker.substring(0, ticker.length() - pattern.length());
            return tickersSet.contains(basePattern);
        }
        return false;
    }

    private boolean checkLeverageDigitPattern(String ticker) {
        if (ticker.endsWith("l") || ticker.endsWith("s")) {
            char penultimateChar = ticker.charAt(ticker.length() - 2);
            return Character.isDigit(penultimateChar);
        }
        return false;
    }

}
