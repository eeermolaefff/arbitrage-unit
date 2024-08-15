package com.stambul.initializers.services;

import com.stambul.library.database.interaction.services.*;
import com.stambul.library.database.objects.dto.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DatabaseService {
    //TODO test new delete functions + separate transfers into another module + locks in scanner
    private final Logger logger = Logger.getLogger(this.getClass());
    private final StampedLock lock = new StampedLock();
    private final CurrenciesService currenciesService;
    private final ContractsService contractsService;
    private final RelationsService relationsService;
    private final ContractTimestampsService contractTimestampsService;
    private final RelationTimestampsService relationTimestampsService;
    private final MarketsService marketsService;
    private final TransferService transferService;
    private final BlockchainsService blockchainsService;

    @Autowired
    public DatabaseService(
            CurrenciesService currenciesService,
            ContractsService contractsService,
            MarketsService marketsService,
            ContractTimestampsService contractTimestampsService,
            RelationTimestampsService relationTimestampsService,
            RelationsService relationsService,
            TransferService transferService,
            BlockchainsService blockchainsService
    ) {
        this.currenciesService = currenciesService;
        this.contractsService = contractsService;
        this.marketsService = marketsService;
        this.relationsService = relationsService;
        this.contractTimestampsService = contractTimestampsService;
        this.relationTimestampsService = relationTimestampsService;
        this.transferService = transferService;
        this.blockchainsService = blockchainsService;
    }

    public List<CurrencyDTO> getAllCurrencies() {
        long stamp = lock.readLock();
        try {
            return currenciesService.getAll();
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public Map<Integer, BlockchainDTO> getBlockchainsMap(Iterable<Integer> ids) {
        long stamp = lock.readLock();
        try {
            return blockchainsService.getMap(ids);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public List<RelationDTO> getRelationsByMarketsIds(int id) {
        long stamp = lock.readLock();
        try {
            return relationsService.getByMarketIds(List.of(id));
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Transactional
    public void insertContracts(
            Map<Integer, CurrencyDTO> currenciesMap,
            Map<Integer, Set<ContractDTO>> contractsMap
    ) {
        long stamp = lock.writeLock();

        List<CurrencyDTO> currenciesToUpdate = new LinkedList<>();
        List<ContractDTO> contractsToAdd = new LinkedList<>();
        List<ContractDTO> contractsToUpdate = new LinkedList<>();
        List<ContractDTO> contractsToDelete = new LinkedList<>();
        List<TimestampDTO> timestampsToAdd = new LinkedList<>();
        List<TimestampDTO> timestampsToUpdate = new LinkedList<>();

        try {
            splitContractsParsingResults(
                    currenciesMap, contractsMap, currenciesToUpdate,
                    contractsToAdd, contractsToUpdate, contractsToDelete,
                    timestampsToAdd, timestampsToUpdate
            );

            currenciesService.updateInDatabase(currenciesToUpdate);

            contractsService.addToDatabase(contractsToAdd);
            contractsService.updateInDatabase(contractsToUpdate);
            contractsService.removeFromDatabase(contractsToDelete);

            contractTimestampsService.addToDatabase(timestampsToAdd);
            contractTimestampsService.updateInDatabase(timestampsToUpdate);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Transactional
    public void insertRelations(
            Map<Integer, CurrencyDTO> currenciesMap,
            Map<Integer, Set<RelationDTO>> relationsMap
    ) {
        long stamp = lock.writeLock();

        List<RelationDTO> relationsToAdd = new LinkedList<>();
        List<RelationDTO> relationsToUpdate = new LinkedList<>();
        List<RelationDTO> relationsToDelete = new LinkedList<>();
        List<TimestampDTO> timestampsToAdd = new LinkedList<>();
        List<TimestampDTO> timestampsToUpdate = new LinkedList<>();

        try {
            splitRelationsParsingResults(
                    currenciesMap, relationsMap,
                    relationsToAdd, relationsToUpdate, relationsToDelete,
                    timestampsToAdd, timestampsToUpdate
            );

            relationsService.addToDatabase(relationsToAdd);
            relationsService.updateInDatabase(relationsToUpdate);
            relationsService.removeFromDatabase(relationsToDelete);

            relationTimestampsService.addToDatabase(timestampsToAdd);
            relationTimestampsService.updateInDatabase(timestampsToUpdate);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Transactional
    public void insertTransfers(Iterable<TransferDTO> results) {
        long stamp = lock.writeLock();
        try {
            Map<TransferDTO, TransferDTO> alreadyExisted = transferService.getByFields(results).stream()
                    .collect(Collectors.toMap(Function.identity(), Function.identity(), (r1, r2) -> r1, TreeMap::new));

            List<TransferDTO> transfersToAdd = new LinkedList<>();
            List<TransferDTO> transfersToUpdate = new LinkedList<>();
            for (TransferDTO newTransfer : results) {
                TransferDTO existedTransfer = alreadyExisted.remove(newTransfer);
                if (existedTransfer == null)
                    transfersToAdd.add(newTransfer);
                else {
                    existedTransfer.updateFields(newTransfer);
                    transfersToUpdate.add(existedTransfer);
                }
            }

            logger.debug("Adding " + transfersToAdd.size() + " new transfers to database");
            transferService.addToDatabase(transfersToAdd);
            logger.debug("Updating " + transfersToUpdate.size() + " already exist transfers");
            transferService.updateInDatabase(transfersToUpdate);
            logger.debug("Removing " + alreadyExisted.size() + " outdated transfers");
            transferService.removeFromDatabase(alreadyExisted.keySet());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Transactional
    public void insertMarkets(Map<Integer, MarketDTO> results) {
        long stamp = lock.writeLock();
        try {
            Map<Integer, MarketDTO> alreadyExisted = marketsService.getMap(results.keySet());
            List<MarketDTO> marketsToAdd = new LinkedList<>();
            List<MarketDTO> marketsToUpdate = new LinkedList<>();

            for (var newMarketEntry : results.entrySet()) {
                MarketDTO oldMarket = alreadyExisted.remove(newMarketEntry.getKey());
                MarketDTO newMarket = newMarketEntry.getValue();
                if (oldMarket == null)  {
                    marketsToAdd.add(newMarket);
                } else {
                    oldMarket.updateFields(newMarket);
                    marketsToUpdate.add(oldMarket);
                }
            }

            logger.debug("Adding " + marketsToAdd.size() + " new markets to database");
            marketsService.addToDatabase(marketsToAdd);
            logger.debug("Updating " + marketsToUpdate.size() + " already exist markets");
            marketsService.updateInDatabase(marketsToUpdate);
            logger.debug("Removing " + alreadyExisted.size() + " outdated markets");
            marketsService.removeFromDatabase(alreadyExisted.values());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Transactional
    public void insertCurrencies(Map<Integer, CurrencyDTO> results) {
        long stamp = lock.writeLock();
        try {
            Map<Integer, CurrencyDTO> alreadyExisted = currenciesService.getMap(results.keySet());
            List<CurrencyDTO> currenciesToAdd = new LinkedList<>();
            List<CurrencyDTO> currenciesToUpdate = new LinkedList<>();

            for (var newCurrencyEntry : results.entrySet()) {
                CurrencyDTO oldCurrency = alreadyExisted.remove(newCurrencyEntry.getKey());
                CurrencyDTO newCurrency = newCurrencyEntry.getValue();
                if (oldCurrency == null) {
                    currenciesToAdd.add(newCurrency);
                } else {
                    oldCurrency.updateFields(newCurrency);
                    currenciesToUpdate.add(oldCurrency);
                }
            }

            logger.debug("Adding " + currenciesToAdd.size() + " new currencies to database");
            currenciesService.addToDatabase(currenciesToAdd);
            logger.debug("Updating " + currenciesToUpdate.size() + " already exist currencies");
            currenciesService.updateInDatabase(currenciesToUpdate);
            logger.debug("Removing " + alreadyExisted.size() + " outdated currencies");
            currenciesService.removeFromDatabase(alreadyExisted.values());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void splitContractsParsingResults(
            Map<Integer, CurrencyDTO> parsedCurrenciesMap,
            Map<Integer, Set<ContractDTO>> parsedContractsMap,
            List<CurrencyDTO> currenciesToUpdate,
            List<ContractDTO> contractsToAdd,
            List<ContractDTO> contractsToUpdate,
            List<ContractDTO> contractsToDelete,
            List<TimestampDTO> timestampsToAdd,
            List<TimestampDTO> timestampsToUpdate
    ) {
        Iterable<Integer> currencyIds = parsedCurrenciesMap.keySet();
        Map<ContractDTO, ContractDTO> existedContracts = contractsService.getByCurrencyIds(currencyIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (r1, r2) -> r1, TreeMap::new));
        Map<TimestampDTO, TimestampDTO> existedTimestamps = contractTimestampsService.getByCurrencyIds(currencyIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (r1, r2) -> r1, TreeMap::new));

        for (int currencyId : currencyIds) {
            CurrencyDTO currency = parsedCurrenciesMap.get(currencyId);
            currenciesToUpdate.add(currency);

            for (ContractDTO parsedContract : parsedContractsMap.get(currencyId)) {
                ContractDTO existedContract = existedContracts.remove(parsedContract);
                if (existedContract != null) {
                    existedContract.updateFields(parsedContract);
                    contractsToUpdate.add(existedContract);
                } else {
                    contractsToAdd.add(parsedContract);
                }
            }

            TimestampDTO timestamp = currency.getContractsUpdatedAt();
            if (existedTimestamps.containsKey(timestamp))   timestampsToUpdate.add(existedTimestamps.get(timestamp));
            else                                            timestampsToAdd.add(timestamp);
        }
        contractsToDelete.addAll(existedContracts.keySet());
    }

    private void splitRelationsParsingResults(
            Map<Integer, CurrencyDTO> parsedCurrenciesMap,
            Map<Integer, Set<RelationDTO>> parsedRelationsMap,
            List<RelationDTO> relationsToAdd,
            List<RelationDTO> relationsToUpdate,
            List<RelationDTO> relationsToDelete,
            List<TimestampDTO> timestampsToAdd,
            List<TimestampDTO> timestampsToUpdate
    ) {
        Iterable<Integer> currencyIds = parsedCurrenciesMap.keySet();
        Map<RelationDTO, RelationDTO> existedRelations = relationsService.getByCurrencyIds(currencyIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (r1, r2) -> r1, TreeMap::new));
        Map<TimestampDTO, TimestampDTO> existedTimestamps = relationTimestampsService.getByCurrencyIds(currencyIds)
                .stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (t1, t2) -> t1, TreeMap::new));

        for  (int currencyId : currencyIds) {
            CurrencyDTO currency = parsedCurrenciesMap.get(currencyId);

            for (RelationDTO parsedRelation : parsedRelationsMap.get(currencyId)) {
                RelationDTO existedRelation = existedRelations.remove(parsedRelation);
                if (existedRelation != null) {
                    existedRelation.updateFields(parsedRelation);
                    relationsToUpdate.add(existedRelation);
                } else {
                    relationsToAdd.add(parsedRelation);
                }
            }

            TimestampDTO timestamp = currency.getRelationsUpdatedAt();
            if (existedTimestamps.containsKey(timestamp))   timestampsToUpdate.add(existedTimestamps.get(timestamp));
            else                                            timestampsToAdd.add(timestamp);
        }
        relationsToDelete.addAll(existedRelations.keySet());
    }
}
