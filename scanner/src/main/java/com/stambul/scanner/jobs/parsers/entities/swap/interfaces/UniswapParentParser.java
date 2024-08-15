package com.stambul.scanner.jobs.parsers.entities.swap.interfaces;

import com.stambul.library.database.objects.dto.*;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.Pair;
import com.stambul.library.tools.StringST;
import com.stambul.scanner.jobs.entities.interfaces.Job;
import com.stambul.scanner.jobs.parsers.entities.interfaces.ParentParser;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.scanner.services.DatabaseService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

public abstract class UniswapParentParser<J extends Job> extends ParentParser<J, SwapDTO> {
    private StringST<Integer> currencyIdByAddress;
    private Map<Integer, ContractDTO> contractByCurrencyId;
    private Integer marketId;

    public UniswapParentParser(
            Class<J> jobClass,
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            String updateDelayISO
    ) {
        super(jobClass, SwapDTO.class, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }

    @Override
    protected Iterable<FieldUpdater> getUpdateTasks() {
        return List.of(
                this::updateCurrencyMap
        );
    };

    protected void updateCurrencyMap() {
        marketId = (Integer) config.get("marketId");
        if (marketId == null)
            throw new RuntimeException("Null marketId");

        Integer chainId = (Integer) config.get("chainId");
        if (chainId == null)
            throw new RuntimeException("Null chainId");

        currencyIdByAddress = new StringST<>();
        contractByCurrencyId = new TreeMap<>();
        for (ContractDTO contract : databaseService.getContractsByBlockchainIds(chainId)) {
            String address = contract.getAddress();
            Integer currencyId = contract.getCurrencyId();
            currencyIdByAddress.put(toKey(address), currencyId);
            contractByCurrencyId.put(currencyId, contract);
        }
    }

    @Override
    protected ParsingResults<SwapDTO> parse() {
        List<SwapDTO> swaps = new LinkedList<>();
        List<Pair<String, Exception>> exceptions = new LinkedList<>();

        ParsingResults<SwapDTO> parsingResults = new ParsingResults<>(
                SwapDTO.class, getClass().getSimpleName()
        );

        for (Object pair : getTickerInfoList()) {
            Map<String, Object> pool = (Map<String, Object>) pair;
            try {
                SwapDTO swap = jsonToSwap(pool);
                if (swap != null)
                    swaps.add(swap);
            } catch (Exception e) {
                Object id = pool.get("id");
                Object ticker = getTicker(pool);

                String message = String.format("Can't convert json to swap: id=%s, ticker=%s", id, ticker);
                RuntimeException root = new RuntimeException(message);
                exceptions.add(new Pair<>(pair.toString(), root));
            }
        }

        parsingResults.addAll(swaps, marketId);
        parsingResults.blockAll(exceptions, marketId);
        return parsingResults;
    }

    private List<Object> getTickerInfoList() {
        String priceInfoURL = (String) config.get("priceInfoURL");
        List<Object> tickerInfoList = new LinkedList<>();

        int first = 1000;
        for (int skip = 0; skip <= 5000; skip += 1000) {
            String query = String.format((String) config.get("query"), skip, first);
            String body = String.format("{\"query\" : \"%s\"}", query);
            Map<String, Object> parsedData = (Map<String, Object>) ioService.parseInfoAsJSON(priceInfoURL, body);
            if (parsedData.containsKey("data")) {
                tickerInfoList.addAll((List<Object>) ioService.extractDataFromJson(parsedData, "data.pools"));
            } else {
                throw new RuntimeException("Parsed data doesn't contains data key: parsedData=" + parsedData);
            }
        }

        return tickerInfoList;
    }

    private Object getTicker(Map<String, Object> jsonPair) {
        Object token0 = ioService.extractDataFromJson(jsonPair, "token0.symbol");
        Object token1 = ioService.extractDataFromJson(jsonPair, "token1.symbol");
        return token0.toString() + "/" + token1.toString();
    }

    private SwapDTO jsonToSwap(Map<String, Object> pool) {
        Map<String, Object> poolDayData = (Map<String, Object>) ((List<Object>) pool.get("poolDayData")).get(0);
        if (!isActual(poolDayData))
            return null;

        String hash = (String) pool.get("id");
        Boolean isActive = true;
        Double basePrice = Double.parseDouble((String) poolDayData.get("token1Price"));
        Double quotePrice = Double.parseDouble((String) poolDayData.get("token0Price"));

        Double feeTier = Double.parseDouble((String) pool.get("feeTier")) / 10000;
        Double liquidity = Double.parseDouble((String) poolDayData.get("liquidity"));
        Double tvlUsd = Double.parseDouble((String) poolDayData.get("tvlUSD"));;
        Double tvlBase = null;
        Double tvlQuote = null;

        Double dailyVolumeUsd = Double.parseDouble((String) poolDayData.get("volumeUSD"));;
        Double dailyVolumeBase = Double.parseDouble((String) poolDayData.get("volumeToken0"));;
        Double dailyVolumeQuote = Double.parseDouble((String) poolDayData.get("volumeToken1"));;

        String tradingType = "dex";

        String baseAsset = (String) ioService.extractDataFromJson(pool, "token0.symbol");
        String quoteAsset = (String) ioService.extractDataFromJson(pool, "token1.symbol");
        String baseAssetAddress = (String) ioService.extractDataFromJson(pool, "token0.id");
        String quoteAssetAddress = (String) ioService.extractDataFromJson(pool, "token1.id");

        String pairTicker = baseAsset + "/" + quoteAsset;
        Integer baseCurrencyId = currencyIdByAddress.get(toKey(baseAssetAddress));
        Integer quoteCurrencyId = currencyIdByAddress.get(toKey(quoteAssetAddress));
        ContractDTO base = contractByCurrencyId.get(baseCurrencyId);
        ContractDTO quote = contractByCurrencyId.get(quoteCurrencyId);;

        validate(pairTicker, baseCurrencyId, quoteCurrencyId, base, quote);

        TradingPairDTO tradingPair = new TradingPairDTO(marketId, baseCurrencyId, quoteCurrencyId, new TickerDTO(pairTicker));

        return new SwapDTO(
                tradingPair, base, quote, isActive, basePrice, quotePrice,
                feeTier, liquidity, hash, tvlUsd, tvlBase, tvlQuote,
                dailyVolumeUsd, dailyVolumeBase, dailyVolumeQuote, tradingType
        );
    }

    private void validate(
            String pairTicker,
            Integer baseCurrencyId, Integer quoteCurrencyId,
            ContractDTO base, ContractDTO quote
    ) {
        if (baseCurrencyId == null || quoteCurrencyId == null) {
            String message = "Null currencyId: pairTicker=%s, baseCurrencyId=%s, quoteCurrencyId=%s";
            throw new RuntimeException(String.format(message, pairTicker, baseCurrencyId, quoteCurrencyId));
        }
        if (base == null || quote == null) {
            String message = "Null contract: pairTicker=%s, base=%s, quote=%s";
            throw new RuntimeException(String.format(message, pairTicker, base, quote));
        }
    }

    private boolean isActual(Map<String, Object> poolDayData) {
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        Calendar receivedDate = Calendar.getInstance();
        receivedDate.setTime(new Date(((int) poolDayData.get("date")) * 1000L));

        return
                currentDate.get(Calendar.DAY_OF_MONTH) == receivedDate.get(Calendar.DAY_OF_MONTH) &&
                currentDate.get(Calendar.MONTH) == receivedDate.get(Calendar.MONTH) &&
                currentDate.get(Calendar.YEAR) == receivedDate.get(Calendar.YEAR);
    }

    private String toKey(String address) {
        return address.toLowerCase();
    }
}
