package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.SwapDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.SwapDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.models.SwapModel;
import com.stambul.library.tools.IterableTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SwapService extends ParentService<SwapModel, SwapDTO> {
    private final TradingPairService tradingPairService;
    private final ContractsService contractsService;

    @Autowired
    public SwapService(
            SwapDAO dao,
            TradingPairService tradingPairService,
            ContractsService contractsService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(dao, batchSize);
        this.tradingPairService = tradingPairService;
        this.contractsService = contractsService;
    }

    @Override
    protected Map<Integer, SwapDTO> toDTOsMap(Iterable<SwapModel> models) {
        Set<Integer> pairIds = new TreeSet<>();
        Set<Integer> contractIds = new TreeSet<>();
        for (SwapModel model : models) {
            contractIds.add(model.getQuoteContractId());
            contractIds.add(model.getBaseContractId());
            pairIds.add(model.getTradingPairId());
        }
        Map<Integer, TradingPairDTO> pairs = tradingPairService.getMap(pairIds);
        Map<Integer, ContractDTO> contracts = contractsService.getMap(contractIds);

        Map<Integer, SwapDTO>  dtos = new TreeMap<>();
        for (SwapModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            SwapDTO swap = modelToDTO(model, pairs, contracts);
            dtos.put(model.getId(), swap);
        }
        return dtos;
    }

    @Override
    protected List<SwapDTO> toDTOsList(Iterable<SwapModel> models) {
        Set<Integer> pairIds = new TreeSet<>();
        Set<Integer> contractIds = new TreeSet<>();
        for (SwapModel model : models) {
            contractIds.add(model.getQuoteContractId());
            contractIds.add(model.getBaseContractId());
            pairIds.add(model.getTradingPairId());
        }
        Map<Integer, TradingPairDTO> pairs = tradingPairService.getMap(pairIds);
        Map<Integer, ContractDTO> contracts = contractsService.getMap(contractIds);

        List<SwapDTO> dtos = new LinkedList<>();
        for (SwapModel model : models) {
            SwapDTO swap = modelToDTO(model, pairs, contracts);
            dtos.add(swap);
        }
        return dtos;
    }

    @Override
    protected Map<Integer, SwapModel> toModelsMap(Iterable<SwapDTO> dtos) {
        Map<TradingPairDTO, TradingPairDTO> pairs = tradingPairService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));
        Map<ContractDTO, ContractDTO> contracts = contractsService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        Map<Integer, SwapModel>  models = new TreeMap<>();
        for (SwapDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            updateFields(dto, pairs, contracts);
            models.put(dto.getId(), new SwapModel(dto));
        }
        return models;
    }

    @Override
    protected List<SwapModel> toModelsList(Iterable<SwapDTO> dtos) {
        Map<TradingPairDTO, TradingPairDTO> pairs = tradingPairService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));
        Map<ContractDTO, ContractDTO> contracts = contractsService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        List<SwapModel>  models = new LinkedList<>();
        for (SwapDTO dto : dtos) {
            updateFields(dto, pairs, contracts);
            models.add(new SwapModel(dto));
        }
        return models;
    }

    @Override
    protected void addValidation(SwapDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null: " + dto);
        TradingPairDTO pair = dto.getTradingPair();
        if (pair == null)
            throw new IllegalArgumentException("DTO trading pair should not be null: " + dto);
        if (pair.getMarketId() == null || pair.getTicker() == null || pair.getBaseAssetId() == null || pair.getQuoteAssetId() == null)
            throw new IllegalArgumentException("DTO market, base, quote ids and ticker should not be null: " + dto);
    }

    @Override
    protected void updateValidation(SwapDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(SwapDTO dto) {
        updateValidation(dto);
    }

    @Override
    protected void beforeAddingActions(Iterable<SwapDTO> dtos) {
        List<TradingPairDTO> pairsList = new LinkedList<>();
        for (SwapDTO dto : dtos)
            pairsList.add(dto.getTradingPair());
        tradingPairService.addNonExistingToDatabase(pairsList);
    }

    @Override
    protected void beforeUpdatingActions(Iterable<SwapDTO> dtos) {
        beforeAddingActions(dtos);
    }

    public List<SwapDTO> getByTradingPairIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((SwapDAO) dao).getByTradingPairsIds(ids));
    }


    private SwapDTO modelToDTO(
            SwapModel model,
            Map<Integer, TradingPairDTO> pairs,
            Map<Integer, ContractDTO> contracts
    ) {
        TradingPairDTO pair = pairs.get(model.getTradingPairId());
        if (pair == null)
            throw new IllegalArgumentException("Trading pair not found for model=" + model);
        ContractDTO quote = contracts.get(model.getQuoteContractId());
        if (quote == null)
            throw new IllegalArgumentException("Contract quote not found for model=" + model);
        ContractDTO base = contracts.get(model.getBaseContractId());
        if (base == null)
            throw new IllegalArgumentException("Contract base not found for model=" + model);

        return new SwapDTO(model, pair, base, quote);
    }

    private void updateFields(
            SwapDTO swapDTO,
            Map<TradingPairDTO, TradingPairDTO> pairs,
            Map<ContractDTO, ContractDTO> contracts
    ) {
        TradingPairDTO pair = swapDTO.getTradingPair();
        if (pair.getId() == null)
            pair = pairs.get(pair);
        if (pair == null)
            throw new IllegalArgumentException("Trading pair not found for swapDTO=" + swapDTO);

        ContractDTO base = swapDTO.getBaseContract();
        if (base.getId() == null)
            base = contracts.get(base);
        if (base == null)
            throw new IllegalArgumentException("Contract base not found for swapDTO=" + swapDTO);

        ContractDTO quote = swapDTO.getQuoteContract();
        if (quote.getId() == null)
            quote = contracts.get(quote);
        if (quote == null)
            throw new IllegalArgumentException("Contract quote not found for swapDTO=" + swapDTO);

        swapDTO.setTradingPair(pair);
        swapDTO.setBaseContract(base);
        swapDTO.setQuoteContract(quote);
    }
}
