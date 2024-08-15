package com.stambul.initializers.jobs.parsers.entities;

import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.library.tools.IdentifiableObject;
import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.exceptions.RebootException;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.handlers.MarketsParsersHandler;
import com.stambul.initializers.jobs.parsers.results.MarketsParsersResults;
import com.stambul.initializers.services.ConfigService;
import com.stambul.library.tools.IOService;
import com.stambul.library.tools.ConvertingTools;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MarketsParser implements SimpleParser<MarketDTO> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final Map<Integer, Double> commissions = new TreeMap<>();
    private final IOService ioService;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;
    private final String apiKey;

    @Autowired
    public MarketsParser(
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

    @Async("marketsParsersExecutor")
    public void parse() {
        logger.debug(String.format("PARSING(%s)", this.getClass().getSimpleName()));

        MarketsParsersResults parsingResults = new MarketsParsersResults();
        try {
            Map<String, Object> config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());

            processManualData();

            parseCmcAPI(parsingResults, (Map<String, Object>) config.get("API"));
            parseCmcWeb(parsingResults, (Map<String, Object>) config.get("WEB"));

            parsingResults.taskDone();

        } catch (Exception interruptedException) {
            parsingResults.setInterruptedException(interruptedException);
        }

        eventPublisher.publishEvent(new ParserFinishEvent<>(this, MarketsParsersHandler.class, parsingResults));
    }

    private void processManualData() {
        Map<String, Object> manualData = (Map<String, Object>) configService.getManualData();
        for (Object obj : (List<Object>) manualData.get("ActualMarkets")) {
            Map<String, Object> market = (Map<String, Object>) obj;
            commissions.put((int) market.get("id"), (double) market.get("spotPercentCommission"));
        }
    }

    private void parseCmcAPI(
            MarketsParsersResults parsingResults,
            Map<String, Object> config
    ) {
        Map<String, Object> response = (Map<String, Object>) parseJSONInfoFromAPI(config);

        for (Object jsonExchangeInfo : (List<Object>) response.get("data")) {
            Map<String, Object> exchangeInfo = (Map<String, Object>) jsonExchangeInfo;

            Integer id = ConvertingTools.toInt(exchangeInfo.get("id"));
            String name = (String) exchangeInfo.get("name");
            String slug = (String) exchangeInfo.get("slug");

            MarketDTO market = new MarketDTO(id, slug, name);
            parsingResults.add(market);
        };
    }

    private Object parseJSONInfoFromAPI(Map<String, Object> config) throws RebootException {
        String url = (String) config.get("URL");
        Map<String, Object> httpProperties = (Map<String, Object>) config.get("httpProperties");
        httpProperties.put("X-CMC_PRO_API_KEY", apiKey);

        ResponseEntity<Resource> response = ioService.request(url, HttpMethod.GET, httpProperties);
        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RebootException("Connection error: " + code);

        ByteArrayOutputStream encodedResponse = ioService.encodeGzip(response);
        return ioService.parseJsonFromByteArrayOutputStream(encodedResponse);
    }

    private void parseCmcWeb(
            MarketsParsersResults parsingResults,
            Map<String, Object> config
    ) {
        for (Object exchangeType : (List<Object>) config.get("exchangeTypes"))
            parse((String) exchangeType, parsingResults, config);
    }

    private void parse(
            String exchangeType,
            MarketsParsersResults parsingResults,
            Map<String, Object> config
    ) {
        Document xmlInfo = parseXmlInfo(exchangeType, config);
        Map<String, Object> jsonInfo = extractJsonInfoFromXML(xmlInfo, "__NEXT_DATA__");
        String jsonPath = "props.pageProps." + (exchangeType.equals("lending") ? "exchange" : "initialData.exchanges");
        List<Object> exchanges = (List<Object>) ioService.extractDataFromJson(jsonInfo, jsonPath);

        for (Object jsonMarket : exchanges) {
            try {
                MarketDTO market = jsonToMarket((Map<String, Object>) jsonMarket);
                parsingResults.add(market);
            } catch (Exception e) {
                int id = ConvertingTools.toInt(((Map<String, Object>) jsonMarket).get("id"));
                parsingResults.block(new IdentifiableObject(id, jsonMarket), e);
            }
        }
    }


    private Document parseXmlInfo(String exchangeType, Map<String, Object> config) throws RebootException {
        String url = exchangeType.equals("cex") ? (String) config.get("URL") : config.get("URL") + exchangeType;
        Map<String, Object> httpProperties = (Map<String, Object>) config.get("httpProperties");

        ResponseEntity<Resource> response = ioService.request(url, HttpMethod.GET, httpProperties);
        HttpStatusCode code = response.getStatusCode();
        if (!code.is2xxSuccessful())
            throw new RebootException("Connection error: " + code);

        ByteArrayOutputStream encodedResponse = ioService.encodeGzip(response);
        return ioService.parseXmlFromString(encodedResponse.toString(StandardCharsets.UTF_8));
    }

    private Map<String, Object> extractJsonInfoFromXML(Document doc, String elementId) {
        Element element = doc.getElementById(elementId);
        String json = element.data();
        return (Map<String, Object>) ioService.parseJsonFromString(json);
    }

    private MarketDTO jsonToMarket(Map<String, Object> exchange) {
        Integer id = ConvertingTools.toInt(exchange.get("id"));
        String slug = (String) exchange.get("slug");
        String fullName = (String) exchange.get("name");
        Double totalVol24h = ConvertingTools.toDouble(exchange.get("totalVol24h"));
        Double score = ConvertingTools.toDouble(exchange.get("score"));
        Double spotPercentCommission = commissions.get(id);
        Double trafficScore = ConvertingTools.toDouble(exchange.get("trafficScore"));
        Integer liquidityScore = ConvertingTools.toInt(exchange.get("liquidity"));
        Integer numMarkets = ConvertingTools.toInt(exchange.get("numMarkets"));
        Integer numCoins = ConvertingTools.toInt(exchange.get("numCoins"));
        String dateLaunched = (String) exchange.get("dateLaunched");

        return new MarketDTO(
                id, slug, fullName, totalVol24h, spotPercentCommission, score, trafficScore,
                liquidityScore, numMarkets, numCoins, dateLaunched
        );
    }

}
