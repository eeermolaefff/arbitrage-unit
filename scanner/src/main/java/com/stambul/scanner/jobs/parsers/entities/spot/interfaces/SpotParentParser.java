package com.stambul.scanner.jobs.parsers.entities.spot.interfaces;

import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.TimeTools;
import com.stambul.scanner.jobs.entities.interfaces.Job;
import com.stambul.scanner.jobs.events.ParserFinishEvent;
import com.stambul.scanner.jobs.events.RebootEvent;
import com.stambul.scanner.jobs.parsers.entities.interfaces.ParentParser;
import com.stambul.scanner.jobs.parsers.entities.interfaces.Parser;
import com.stambul.scanner.jobs.parsers.results.ParsingResults;
import com.stambul.scanner.services.ConfigService;
import com.stambul.scanner.services.DatabaseService;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class SpotParentParser<J extends Job, D extends DTO<D>> extends ParentParser<J, D> {
    protected Map<String, RelationDTO> relationsMap;
    protected Map<String, Object> exchangeInfo;

    public SpotParentParser(
            Class<J> jobClass,
            Class<D> dataClass,
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            String updateDelayISO
    ) {
        super(jobClass, dataClass, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }

    @Override
    protected Iterable<FieldUpdater> getUpdateTasks() {
        return List.of(
                this::updateExchangeInfoMap,
                this::updateRelationsMap
        );
    };

    protected abstract void updateExchangeInfoMap();

    protected void updateRelationsMap() {
        int marketId = (int) config.get("marketId");
        String exchangeType = (String) config.get("exchangeType");
        String exchangeCategory = (String) config.get("exchangeCategory");

        relationsMap = new TreeMap<>();
        for (RelationDTO relation : databaseService.getRelationsByMarketsIds(marketId)) {
            String ticker = relation.getTicker().getTicker().toLowerCase();
            if (
                    relation.getExchangeType().equals(exchangeType) &&
                    relation.getExchangeCategory().equals(exchangeCategory) &&
                    relation.getMarketId().equals(marketId)
            ) {
                relationsMap.put(ticker, relation);
            }
        };
    }

    protected TradingPairDTO getTradingPair(
            String pairTicker,
            String baseAsset,
            String quoteAsset
    ) {
        RelationDTO baseRelation = relationsMap.get(baseAsset.toLowerCase());
        RelationDTO quoteRelation = relationsMap.get(quoteAsset.toLowerCase());

        validate(baseRelation, quoteRelation, pairTicker, baseAsset, quoteAsset);

        return new TradingPairDTO(
                baseRelation.getMarketId(),
                baseRelation.getCurrencyId(),
                quoteRelation.getCurrencyId(),
                new TickerDTO(pairTicker)
        );
    }

    private void validate(
            RelationDTO baseRelation,
            RelationDTO quoteRelation,
            String pairTicker,
            String baseAsset,
            String quoteAsset
    ) {
        if (baseRelation == null) {
            String message = "Null base relation: pairTicker=%s, baseAsset=%s";
            throw new RuntimeException(String.format(message, pairTicker, baseAsset));
        }
        if (quoteRelation == null) {
            String message = "Null quote relation: pairTicker=%s, quoteAsset=%s";
            throw new RuntimeException(String.format(message, pairTicker, quoteAsset));
        }

        if (!baseRelation.getMarketId().equals(quoteRelation.getMarketId())) {
            String message = "Market ids mismatch: baseRelation=%s, quoteRelation=%, pairTicker=%s";
            throw new RuntimeException(String.format(message, baseRelation, quoteRelation, pairTicker));
        }

        if (!baseRelation.getExchangeType().equals(quoteRelation.getExchangeType())) {
            String message = "Exchange types mismatch: baseRelation=%s, quoteRelation=%, pairTicker=%s";
            throw new RuntimeException(String.format(message, baseRelation, quoteRelation, pairTicker));
        }

        if (!baseRelation.getExchangeCategory().equals(quoteRelation.getExchangeCategory())) {
            String message = "Exchange categories mismatch: baseRelation=%s, quoteRelation=%, pairTicker=%s";
            throw new RuntimeException(String.format(message, baseRelation, quoteRelation, pairTicker));
        }
    }
}
