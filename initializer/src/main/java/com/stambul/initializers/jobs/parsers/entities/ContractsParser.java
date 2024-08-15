package com.stambul.initializers.jobs.parsers.entities;

import com.stambul.initializers.jobs.events.ParserFinishEvent;
import com.stambul.initializers.jobs.exceptions.RebootException;
import com.stambul.initializers.jobs.parsers.entities.interfaces.BatchParser;
import com.stambul.initializers.jobs.parsers.handlers.ContractsParsersHandler;
import com.stambul.initializers.jobs.parsers.results.ContractsParsersResults;
import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.tools.IOService;

import com.stambul.initializers.services.ConfigService;
import com.stambul.library.tools.ConvertingTools;
import org.apache.log4j.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ContractsParser implements BatchParser<CurrencyDTO, ContractDTO> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final IOService ioService;
    private final ConfigService configService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ContractsParser(
            IOService ioService,
            ConfigService configService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.ioService = ioService;
        this.configService = configService;
        this.eventPublisher = eventPublisher;
    }

    @Async("contractParsersExecutor")
    public void parse(List<CurrencyDTO> batch, long delayMills) {
        sleep(delayMills);
        logger.debug(String.format("PARSING(%s): %s", this.getClass().getSimpleName(), batch));

        ContractsParsersResults parsingResults = new ContractsParsersResults();

        for (CurrencyDTO currency : batch) {
            try {
                putContractAndCurrencyInfo(currency, parsingResults);
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

        eventPublisher.publishEvent(new ParserFinishEvent<>(this, ContractsParsersHandler.class, parsingResults));
    }

    private void putContractAndCurrencyInfo(
            CurrencyDTO currency,
            ContractsParsersResults parsingResults
    ) {
        Document xmlInfo = parseXmlInfo(currency.getSlug());
        Map<String, Object> jsonInfo = extractJsonInfoFromXML(xmlInfo, "__NEXT_DATA__");
        Map<String, Object> details = (Map<String, Object>) ioService.extractDataFromJson(
                jsonInfo, "props.pageProps.detailRes.detail"
        );

        List<ContractDTO> batch = extractContractsList(currency, (List<Object>) details.get("platforms"));
        CurrencyDTO updatedCurrency = updateCurrency(currency, details);

        parsingResults.add(updatedCurrency, batch);
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

    private CurrencyDTO updateCurrency(CurrencyDTO currency, Map<String, Object> details) {
        String category = (String) details.get("category");
        Double cexVolumeUsd = ConvertingTools.toDouble(details.get("cexVolume"));
        Double dexVolumeUsd = ConvertingTools.toDouble(details.get("dexVolume"));
        Double marketCapUsd = ConvertingTools.toDouble(ioService.extractDataFromJson(details, "statistics.marketCap"));
        TimestampDTO timestamp = currency.getContractsUpdatedAt();
        if (timestamp == null)
            timestamp = new TimestampDTO(currency.getId());

        currency.setCategory(category);
        currency.setCexVolumeUsd(cexVolumeUsd);
        currency.setDexVolumeUsd(dexVolumeUsd);
        currency.setMarketCapUsd(marketCapUsd);
        currency.setContractsUpdatedAt(timestamp);

        return currency;
    }

    private List<ContractDTO> extractContractsList(CurrencyDTO currency, List<Object> platforms) {
        List<ContractDTO> contracts = new LinkedList<>();
        for (Object platform : platforms) {
            ContractDTO contract = jsonToContract((Map<String, Object>) platform, currency);
            contracts.add(contract);
        }
        return contracts;
    }

    private ContractDTO jsonToContract(Map<String, Object> platform, CurrencyDTO currency) {
        try {
            String address = contractPreparation(platform.get("contractAddress"));
            Integer blockchainId = ConvertingTools.toInt(platform.get("contractPlatformId"));
            Integer baseCoinId = ConvertingTools.toInt(platform.get("platformCryptoId"));
            String blockchain = (String) platform.get("contractPlatform");
            return new ContractDTO(new BlockchainDTO(blockchainId, blockchain, baseCoinId), currency.getId(), address);
        } catch (Exception e) {
            String message = "Problem during json to ContractDTO conversion: ";
            message += String.format("platform=%s, currency=%s", platform, currency);
            throw new RuntimeException(message, e);
        }
    }

    private String contractPreparation(Object contractFromJson) {
        if (contractFromJson == null)
            throw new RuntimeException("Null contract received");
        String contract = (String) contractFromJson;
        if (contract.startsWith("http")) {
            logger.error("Strange contract: " + contract);
            int contractFrom = contract.lastIndexOf('/') + 1;
            if (contractFrom < contract.length() - 1)
                contract = contract.substring(contractFrom);
            logger.error("Cut strange contract: " + contract);
        }
        return contract.toLowerCase();
    }

    private Document parseXmlInfo(String slug) {
        Map<String, Object> config = (Map<String, Object>) configService.getConfig(this.getClass().getSimpleName());
        String url = String.format((String) config.get("URL"), slug);
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
}
