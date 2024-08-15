package com.stambul.initializers.jobs.parsers.entities;

import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.tools.IdentifiableObject;
import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.exceptions.RebootException;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.handlers.CurrenciesParsersHandler;
import com.stambul.initializers.jobs.parsers.results.CurrenciesParsersResults;
import com.stambul.initializers.services.ConfigService;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.ConvertingTools;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CurrenciesParser implements SimpleParser<CurrencyDTO> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final IOService ioService;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;
    private final String apiKey;

    @Autowired
    public CurrenciesParser(
            IOService ioService,
            ConfigService configService,
            ApplicationEventPublisher eventPublisher,
            @Value("${coinmarketcap.api.key}") String apiKey
    ) {
        this.ioService = ioService;
        this.configService = configService;
        this.eventPublisher = eventPublisher;
        this.apiKey = apiKey;
    }

    @Async("currenciesParsersExecutor")
    public void parse() {
        logger.debug(String.format("PARSING(%s)", this.getClass().getSimpleName()));

        CurrenciesParsersResults parsingResults = new CurrenciesParsersResults();
        try {
            Map<String, Object> config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());

            parseCmcAPI(parsingResults, "fiat", (Map<String, Object>) config.get("API"));
            parseCmcAPI(parsingResults, "crypto", (Map<String, Object>) config.get("API"));
            parseCmcWeb(parsingResults, (Map<String, Object>) config.get("WEB"));

            parsingResults.taskDone();

        } catch (Exception interruptedException) {
            parsingResults.setInterruptedException(interruptedException);
        }

        eventPublisher.publishEvent(new ParserFinishEvent<>(this, CurrenciesParsersHandler.class, parsingResults));
    }

    public void parseCmcWeb(
            CurrenciesParsersResults parsingResults,
            Map<String, Object> config
    ) {
        Map<String, Object> currenciesList = (Map<String, Object>) parseCurrenciesListAsJSON(config);
        Map<String, Integer> fieldsPositions = getFieldsPositions((List<Object>) currenciesList.get("fields"));

        for (Object jsonCurrency : (List<Object>) currenciesList.get("values")) {
            try {
                CurrencyDTO currency = jsonToCurrency((List<Object>) jsonCurrency, fieldsPositions);
                parsingResults.add(currency);
            } catch (Exception e) {
                int id = ConvertingTools.toInt(((List<Object>) jsonCurrency).get(fieldsPositions.get("id")));
                parsingResults.block(new IdentifiableObject(id, jsonCurrency), e);
            }
        }
    }

    public void parseCmcAPI(
            CurrenciesParsersResults parsingResults,
            String specifier, Map<String, Object> config
    ) {
        boolean isFiat = specifier.equals("fiat");
        Map<String, Object> response = (Map<String, Object>) parseJSONInfoFromAPI(config, specifier);

        for (Object jsonCurrency : (List<Object>) response.get("data")) {
            try {
                CurrencyDTO currency = jsonToCurrency((Map<String, Object>) jsonCurrency, isFiat);
                parsingResults.add(currency);
            } catch (Exception e) {
                int id = ConvertingTools.toInt(((Map<String, Object>) jsonCurrency).get("id"));
                parsingResults.block(new IdentifiableObject(id, jsonCurrency), e);
            }
        };
    }

    private Object parseJSONInfoFromAPI(Map<String, Object> config, String specifier) throws RebootException {
        String url = (String) ((Map<String, Object>) config.get("URL")).get(specifier);
        Map<String, Object> httpProperties = (Map<String, Object>) config.get("httpProperties");
        httpProperties.put("X-CMC_PRO_API_KEY", apiKey);

        ResponseEntity<Resource> response = ioService.request(url, HttpMethod.GET, httpProperties);
        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RebootException("Connection error: " + code);

        ByteArrayOutputStream encodedResponse = ioService.encodeGzip(response);
        return ioService.parseJsonFromByteArrayOutputStream(encodedResponse);
    }

    private CurrencyDTO jsonToCurrency(Map<String, Object> currencyInfo, boolean isFiat) {
        int id = ConvertingTools.toInt(currencyInfo.get("id"));
        String fullName = (String) currencyInfo.get("name");

        String slug, category = null;
        boolean isActive;
        if (isFiat) {
            category = "fiat";
            slug = currencyInfo.get("symbol").toString().toLowerCase() + "_fiat";
            isActive = true;
        } else {
            slug = (String) currencyInfo.get("slug");
            isActive = ConvertingTools.toInt(currencyInfo.get("is_active")) == 1;
        }

        return new CurrencyDTO(id, slug, fullName, isActive, category);
    }

    private CurrencyDTO jsonToCurrency(List<Object> jsonCurrency, Map<String, Integer> fieldsPositions) {
        Integer id = ConvertingTools.toInt(jsonCurrency.get(fieldsPositions.get("id")));
        String slug = (String) jsonCurrency.get(fieldsPositions.get("slug"));
        String fullName = (String) jsonCurrency.get(fieldsPositions.get("name"));
        Boolean isActive = ConvertingTools.toInt(jsonCurrency.get(fieldsPositions.get("is_active"))) == 1;

        return new CurrencyDTO(id, slug, fullName, isActive, null);
    }

    private Object parseCurrenciesListAsJSON(Map<String, Object> config) throws RebootException {
        String url = (String) config.get("URL");
        Map<String, Object> httpProperties = (Map<String, Object>) config.get("httpProperties");

        ResponseEntity<Resource> response = ioService.request(url, HttpMethod.GET, httpProperties);
        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RebootException("Connection error: " + code);

        ByteArrayOutputStream encodedResponse = ioService.encodeGzip(response);
        return ioService.parseJsonFromByteArrayOutputStream(encodedResponse);
    }

    private Map<String, Integer> getFieldsPositions(List<Object> fields) {
        Map<String, Integer> fieldsPos = new TreeMap<>();
        for (int pos = 0; pos < fields.size(); pos++)
            fieldsPos.put((String) fields.get(pos), pos);
        return fieldsPos;
    }

}
