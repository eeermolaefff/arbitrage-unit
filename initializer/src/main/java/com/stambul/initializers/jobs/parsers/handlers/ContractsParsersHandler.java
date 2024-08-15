package com.stambul.initializers.jobs.parsers.handlers;

import com.stambul.initializers.services.DatabaseService;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.interfaces.Identifiable;
import com.stambul.initializers.jobs.entities.ContractsJob;
import com.stambul.initializers.services.BlacklistService;
import com.stambul.initializers.jobs.parsers.initializers.ContractsParsersInitializer;
import com.stambul.initializers.jobs.parsers.handlers.interfaces.ParentParsersHandler;
import com.stambul.initializers.jobs.parsers.results.ContractsParsersResults;
import com.stambul.library.tools.IdentifiablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContractsParsersHandler extends ParentParsersHandler<ContractsParsersHandler, ContractsParsersResults> {
    private final DatabaseService databaseService;

    @Autowired
    public ContractsParsersHandler(
            DatabaseService databaseService,
            BlacklistService blacklistService,
            ApplicationEventPublisher publisher,
            ContractsParsersInitializer initializer,
            @Qualifier("contractParsersExecutor") ThreadPoolTaskExecutor executor,
            @Value("${coinmarketcap.logger.info.max.message.length}") int maxMessageLength
    ) {
        super(ContractsJob.class, publisher, executor, initializer, blacklistService, maxMessageLength);
        this.databaseService = databaseService;
    }

    @Override
    protected void insert(ContractsParsersResults result) {
        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();
        Map<Integer, Set<ContractDTO>> contractsMap = result.getSecondaryMap();

        databaseService.insertContracts(currenciesMap, contractsMap);
    }

    @Override
    protected ContractsParsersResults filterResults(ContractsParsersResults result) {
        if (result.isParsingResultEmpty())
            return result;

        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();
        Map<Integer, Set<ContractDTO>> contractsMap = result.getSecondaryMap();

        Map<Integer, Set<Integer>> blacklist = getBlacklist();
        List<Integer> currenciesToDelete = new LinkedList<>();

        for (int currencyId : contractsMap.keySet()) {
            Set<Integer> blockedBlockchainIds = blacklist.get(currencyId);
            if (blockedBlockchainIds == null)
                continue;

            Set<ContractDTO> contracts = contractsMap.get(currencyId);
            contracts.removeIf(contract -> blockedBlockchainIds.contains(contract.getBlockchain().getId()));
            if (contracts.isEmpty())
                currenciesToDelete.add(currencyId);
        }

        for (int currencyId : currenciesToDelete) {
            currenciesMap.remove(currencyId);
            contractsMap.remove(currencyId);
        }

        return result;
    }

    private Map<Integer, Set<Integer>> getBlacklist() {
        Map<String, Object> secondaryMap = blacklistService.getBlacklist(job.getName(), "secondary");
        Map<Integer, Set<Integer>> result = new TreeMap<>();

        for (String id : secondaryMap.keySet()) {
            Set<Integer> blockchainIds = new TreeSet<>();

            for (Object block : (List<Object>) secondaryMap.get(id)) {
                Map<String, Object> blockedObject = (Map<String, Object>) block;
                for (Object blockchainId : (List<Object>) blockedObject.get("blockchainIds"))
                    blockchainIds.add((int) blockchainId);
            }

            result.put(Integer.parseInt(id), blockchainIds);
        }

        return result;
    }

    @Override
    protected ContractsParsersResults[] split(ContractsParsersResults result) {
        Map<Integer, CurrencyDTO> currenciesMap = result.getPrimaryMap();
        Map<Integer, Set<ContractDTO>> contractsMap = result.getSecondaryMap();

        int totalSize = 0;
        for (int currencyId : currenciesMap.keySet())
            totalSize += contractsMap.get(currencyId).size();

        if (totalSize <= 1)
            return new ContractsParsersResults[] { result };

        ContractsParsersResults[] results = new ContractsParsersResults[] {
                new ContractsParsersResults(),  new ContractsParsersResults()
        };

        int splitSize = totalSize / 2;
        int activeIdx = 0;
        int counter = 0;

        for (int currencyId : currenciesMap.keySet()) {
            CurrencyDTO currency = currenciesMap.get(currencyId);
            Set<ContractDTO> contracts = contractsMap.get(currencyId);

            if (counter + contracts.size() < splitSize) {
                results[activeIdx].add(currency, contracts);
                counter += contracts.size();
            } else {
                for (ContractDTO contract : contracts) {
                    results[activeIdx].add(currency, contract);
                    if (++counter == splitSize)
                        activeIdx++;
                }
            }
        }

        return results;
    };

    @Override
    protected Map<String, Object> getBlockedObjectAsJson(Identifiable blockedObject, Exception cause) {
        Map<String, Object> blockedObjectAsJson = super.getBlockedObjectAsJson(blockedObject, cause);

        if (blockedObject instanceof IdentifiablePair<?, ?> pair) {
            List<Integer> blockchainIds = new LinkedList<>();
            for (ContractDTO contract : (Set<ContractDTO>) pair.getSecond())
                blockchainIds.add(contract.getBlockchain().getId());
            blockedObjectAsJson.put("blockchainIds", blockchainIds);
        }

        return blockedObjectAsJson;
    }
}

