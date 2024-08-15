package com.stambul.scanner.jobs.parsers.entities.spot;


import com.stambul.library.tools.Pair;
import com.stambul.scanner.jobs.parsers.entities.spot.interfaces.SpotParentParser;
import com.stambul.scanner.services.DatabaseService;
import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.scanner.jobs.entities.spot.BybitJob;
import com.stambul.scanner.jobs.parsers.entities.interfaces.ParentParser;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.library.tools.IOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BybitParser extends SpotParentParser<BybitJob, OrderbookDTO> {

    @Autowired
    public BybitParser(
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            @Value("${bybit.spot.update.delay.iso}") String updateDelayISO
    ) {
        super(BybitJob.class, OrderbookDTO.class, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }


    @Override
    protected void updateExchangeInfoMap() {
        String exchangeInfoURL = (String) config.get("exchangeInfoURL");
        Map<String, Object> parsedJsonInfo = (Map<String, Object>) ioService.parseInfoAsJSON(exchangeInfoURL);

        exchangeInfo = new TreeMap<>();
        for (Object pair : (List<Object>) ioService.extractDataFromJson(parsedJsonInfo, "result.list")) {
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
            if (isLeverageToken(pairTicker))
                continue;
            Map<String, Object> pairExchangeInfo = (Map<String, Object>) exchangeInfo.get(pairTicker);
            if (!pairExchangeInfo.get("status").equals("Trading"))
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
        Map<String, Object> parsedJsonInfo = (Map<String, Object>) ioService.parseInfoAsJSON(priceInfoURL);

        Map<String, Object> tickerInfo = new TreeMap<>();
        for (Object pair : (List<Object>) ioService.extractDataFromJson(parsedJsonInfo, "result.list")) {
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
        Boolean isActive = pairExchangeInfo.get("status").equals("Trading");
        Double bidPrice = Double.parseDouble((String) pairTickerInfo.get("bid1Price"));
        Double askPrice = Double.parseDouble((String) pairTickerInfo.get("ask1Price"));
        Double bidQty = Double.parseDouble((String) pairTickerInfo.get("bid1Size"));
        Double askQty = Double.parseDouble((String) pairTickerInfo.get("ask1Size"));

        Double dailyVolumeUsd = Double.parseDouble((String) pairTickerInfo.get("volume24h"));
        Double dailyVolumeBase = null;
        Double dailyVolumeQuote = null;

        Boolean isSpotTradingAllowed = true;
        Boolean isMarginTradingAllowed = !pairExchangeInfo.get("marginTrading").equals("none");
        String tradingType = "cex";

        String baseAsset = (String) pairExchangeInfo.get("baseCoin");
        String quoteAsset = (String) pairExchangeInfo.get("quoteCoin");
        TradingPairDTO tradingPair = getTradingPair(pairTicker, baseAsset, quoteAsset);

        return new OrderbookDTO(
                tradingPair, isActive, bidPrice, askPrice, bidQty, askQty, dailyVolumeUsd,
                dailyVolumeBase, dailyVolumeQuote, isSpotTradingAllowed, isMarginTradingAllowed, tradingType
        );
    }

    private boolean isLeverageToken(String input) {
        char[] charArray = input.toLowerCase().toCharArray();
        for (int i = 0; i < charArray.length; i++)
            if (Character.isDigit(charArray[i])) {
                if (i + 1 < charArray.length) {
                    if (charArray[i + 1] == 'l' || charArray[i + 1] == 's')
                        return true;
                }
            }
        return false;
    }
}
