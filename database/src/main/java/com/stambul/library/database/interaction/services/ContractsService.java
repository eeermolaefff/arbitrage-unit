package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.ContractDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.models.ContractModel;
import com.stambul.library.tools.IterableTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ContractsService extends ParentService<ContractModel, ContractDTO> {
    private final BlockchainsService blockchainsService;

    @Autowired
    public ContractsService(
            ContractDAO contractDAO,
            BlockchainsService blockchainsService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(contractDAO, batchSize);
        this.blockchainsService = blockchainsService;
    }

    @Override
    protected Map<Integer, ContractDTO> toDTOsMap(Iterable<ContractModel> models) {
        Map<Integer, BlockchainDTO> blockchains = blockchainsService.getMap();

        Map<Integer, ContractDTO>  dtos = new TreeMap<>();
        for (ContractModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            BlockchainDTO blockchain = blockchains.get(model.getBlockchainId());
            if (blockchain == null)
                throw new IllegalArgumentException("Blockchain not found for model=" + model);

            dtos.put(model.getId(), new ContractDTO(model, blockchain));
        }
        return dtos;
    }

    @Override
    protected List<ContractDTO> toDTOsList(Iterable<ContractModel> models) {
        Map<Integer, BlockchainDTO> blockchains = blockchainsService.getMap();

        List<ContractDTO>  dtos = new LinkedList<>();
        for (ContractModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            BlockchainDTO blockchain = blockchains.get(model.getBlockchainId());
            if (blockchain == null)
                throw new IllegalArgumentException("Blockchain not found for model=" + model);

            dtos.add(new ContractDTO(model, blockchain));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, ContractModel> toModelsMap(Iterable<ContractDTO> dtos) {
        Map<String, BlockchainDTO> blockchains = blockchainsService.getAll().stream()
                .collect(Collectors.toMap(BlockchainDTO::getName, Function.identity()));

        Map<Integer, ContractModel>  models = new TreeMap<>();
        for (ContractDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);

            BlockchainDTO blockchain = dto.getBlockchain();
            if (blockchain.getId() == null)
                blockchain = blockchains.get(dto.getBlockchain().getName());
            if (blockchain == null)
                throw new IllegalArgumentException("Blockchain not found for dto=" + dto);

            models.put(dto.getId(), new ContractModel(dto, blockchain));
        }
        return models;
    }

    @Override
    protected List<ContractModel> toModelsList(Iterable<ContractDTO> dtos) {
        Map<String, BlockchainDTO> blockchains = blockchainsService.getAll().stream()
                .collect(Collectors.toMap(BlockchainDTO::getName, Function.identity()));

        List<ContractModel>  models = new LinkedList<>();
        for (ContractDTO dto : dtos) {
            BlockchainDTO blockchain = dto.getBlockchain();
            if (blockchain.getId() == null)
                blockchain = blockchains.get(dto.getBlockchain().getName());
            if (blockchain == null)
                throw new IllegalArgumentException("Blockchain not found for dto=" + dto);

            models.add(new ContractModel(dto, blockchain));
        }
        return models;
    }

    @Override
    protected void addValidation(ContractDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Contract should not be null");
        if (dto.getAddress() == null || dto.getBlockchain() == null || dto.getCurrencyId() == null) {
            String message = "Contract address, blockchain and currency id should not be null: " + dto;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(ContractDTO dto) {
        addValidation(dto);
        if (dto.getId() == null )
            throw new IllegalArgumentException("Contract id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(ContractDTO dto) {
        updateValidation(dto);
    }

    @Override
    protected void beforeAddingActions(Iterable<ContractDTO> dtos) {
        List<BlockchainDTO> blockchains = StreamSupport.stream(dtos.spliterator(), false)
                .map(ContractDTO::getBlockchain).toList();
        blockchainsService.addNonExistingToDatabase(blockchains);
    }

    @Override
    protected void beforeUpdatingActions(Iterable<ContractDTO> dtos) {
        beforeAddingActions(dtos);
    }


    public List<ContractDTO> getByCurrencyIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((ContractDAO) dao).getByCurrencyId(ids));
    }

    public List<ContractDTO> getByBlockchainIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((ContractDAO) dao).getByBlockchainId(ids));
    }
}
