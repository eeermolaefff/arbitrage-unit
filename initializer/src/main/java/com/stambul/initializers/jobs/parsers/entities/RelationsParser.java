package com.stambul.initializers.jobs.parsers.entities;

import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.exceptions.RebootException;
import com.stambul.initializers.jobs.parsers.entities.interfaces.BatchParser;
import com.stambul.initializers.jobs.parsers.handlers.RelationsParsersHandler;
import com.stambul.initializers.jobs.parsers.results.RelationsParsersResults;
import com.stambul.initializers.services.ConfigService;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.ConvertingTools;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
public class RelationsParser implements BatchParser<CurrencyDTO, RelationDTO> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final IOService ioService;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public RelationsParser(
            IOService ioService,
            ConfigService configService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.ioService = ioService;
        this.configService = configService;
        this.eventPublisher = eventPublisher;
    }

    @Async("relationsParsersExecutor")
    public void parse(List<CurrencyDTO> batch, long delayMills) {
        sleep(delayMills);
        logger.debug(String.format("PARSING(%s): %s", this.getClass().getSimpleName(), batch));

        RelationsParsersResults parsingResults = new RelationsParsersResults();

        for (CurrencyDTO currency : batch) {
            try {
                putRelationAndCurrencyInfo(currency, parsingResults);
                parsingResults.taskDone();
            } catch (RebootException | ResourceAccessException e) {
                parsingResults.setInterruptedException(e);
                break;
            } catch (HttpClientErrorException e) {
                int errorCode = e.getStatusCode().value();
                if (errorCode == 408 || errorCode == 429) {
                    parsingResults.setInterruptedException(e);
                    break;
                } else {
                    parsingResults.block(currency, e);
                    parsingResults.taskDone();
                }
            } catch (Exception e) {
                parsingResults.block(currency, e);
                parsingResults.taskDone();
            }
        }

        eventPublisher.publishEvent(new ParserFinishEvent<>(this, RelationsParsersHandler.class, parsingResults));
    }

    private void putRelationAndCurrencyInfo(
            CurrencyDTO currency,
            RelationsParsersResults parsingResults
    ) {
        Map<String, Object> config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());

        int limit = (Integer) config.get("parsingLimit");
        for (Object type : (List<Object>) config.get("types")) {
            for (Object category : (List<Object>) config.get("categories")) {
                for (int start = 1; true; start += limit) {
                    Map<String, Object> jsonInfo = (Map<String, Object>) parseJSONInfo(
                            config, currency.getSlug(), start, limit, category, type
                    );
                    Object marketPairs = ioService.extractDataFromJson(jsonInfo, "data.marketPairs");
                    if (marketPairs == null)
                        break;

                    extractRelationsList((List<Object>) marketPairs, currency, parsingResults);
                }
            }
        }
    }

    private void extractRelationsList(
            List<Object> marketPairs,
            CurrencyDTO currency,
            RelationsParsersResults parsingResults
    ) {
        TimestampDTO timestamp = currency.getRelationsUpdatedAt();
        if (timestamp == null)
            timestamp = new TimestampDTO(currency.getId());
        currency.setRelationsUpdatedAt(timestamp);

        for (Object marketPair : marketPairs)
            jsonToRelations((Map<String, Object>) marketPair, currency, parsingResults);

    }

    private void jsonToRelations(
            Map<String, Object> marketPair,
            CurrencyDTO currency,
            RelationsParsersResults parsingResults
    ) {
        try {
            Object[] results = initAdditionalCurrencyAndRelations(marketPair, currency.getId());

            CurrencyDTO additionalCurrency = (CurrencyDTO) results[0];
            RelationDTO mainRelation = (RelationDTO) results[1];
            RelationDTO additionalRelation = (RelationDTO) results[2];

            parsingResults.add(currency, mainRelation);
            parsingResults.add(additionalCurrency, additionalRelation);

        } catch (Exception e) {
            String message = "Problem during json to MarketCurrencyRelationDTO conversion: ";
            message += String.format("marketPair=%s, currency=%s", marketPair, currency);
            throw new RuntimeException(message, e);
        }
    }

    private Object[] initAdditionalCurrencyAndRelations(Map<String, Object> marketPair, int mainCurrencyId) {

        int baseId = ConvertingTools.toInt(marketPair.get("baseCurrencyId"));
        int quoteId = ConvertingTools.toInt(marketPair.get("quoteCurrencyId"));
        TickerDTO baseTicker = new TickerDTO((String) marketPair.get("baseSymbol"));
        TickerDTO quoteTicker = new TickerDTO((String) marketPair.get("quoteSymbol"));
        int marketId = ConvertingTools.toInt(marketPair.get("exchangeId"));
        String exchangeType = (String) marketPair.get("centerType");
        String exchangeCategory = (String) marketPair.get("category");

        CurrencyDTO additionalCurrency = new CurrencyDTO();
        RelationDTO mainRelation = new RelationDTO(marketId, exchangeType, exchangeCategory);
        RelationDTO additionalRelation = new RelationDTO(marketId, exchangeType, exchangeCategory);

        if (mainCurrencyId == baseId) {
            additionalCurrency.setId(quoteId);

            mainRelation.setCurrencyId(baseId);
            mainRelation.setTicker(baseTicker);

            additionalRelation.setCurrencyId(quoteId);
            additionalRelation.setTicker(quoteTicker);
        } else {
            additionalCurrency.setId(baseId);

            mainRelation.setCurrencyId(quoteId);
            mainRelation.setTicker(quoteTicker);

            additionalRelation.setCurrencyId(baseId);
            additionalRelation.setTicker(baseTicker);
        }

        return new Object[] { additionalCurrency, mainRelation, additionalRelation };
    }

    private void sleep(long delayMills) {
        if (delayMills == 0)
            return;
        try {
            Thread.sleep(delayMills);
        } catch (InterruptedException e) {
            logger.error("Sleep between batches process was interrupted", e);
        }
    }

    private Object parseJSONInfo(Map<String, Object> config, Object... formatArgs) {
        String url = String.format((String) config.get("URL"), formatArgs);
        Map<String, Object> httpProperties = (Map<String, Object>) config.get("httpProperties");

        ResponseEntity<Resource> response = ioService.request(url, HttpMethod.GET, httpProperties);
        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RebootException("Connection error: " + code);

        ByteArrayOutputStream encodedResponse = ioService.encodeGzip(response);
        return ioService.parseJsonFromByteArrayOutputStream(encodedResponse);
    }
}
