package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.SwapDAO;
import com.stambul.library.database.interaction.dao.TransferDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.*;
import com.stambul.library.database.objects.models.SwapModel;
import com.stambul.library.database.objects.models.TransferModel;
import com.stambul.library.tools.IterableTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Block;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransferService extends ParentService<TransferModel, TransferDTO> {
    private final RelationsService relationService;
    private final BlockchainsService blockchainsService;

    @Autowired
    public TransferService(
            TransferDAO dao,
            RelationsService relationService,
            BlockchainsService blockchainsService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(dao, batchSize);
        this.relationService = relationService;
        this.blockchainsService = blockchainsService;
    }

    @Override
    protected Map<Integer, TransferDTO> toDTOsMap(Iterable<TransferModel> models) {
        Set<Integer> relationIds = new TreeSet<>();
        Set<Integer> blockchainIds = new TreeSet<>();
        for (TransferModel model : models) {
            relationIds.add(model.getMarketCurrencyRelationId());
            blockchainIds.add(model.getBlockchainId());
        }
        Map<Integer, RelationDTO> relations = relationService.getMap(relationIds);
        Map<Integer, BlockchainDTO> blockchains = blockchainsService.getMap(blockchainIds);

        Map<Integer, TransferDTO> dtos = new TreeMap<>();
        for (TransferModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            TransferDTO transfer = modelToDTO(model, relations, blockchains);
            dtos.put(model.getId(), transfer);
        }
        return dtos;
    }

    @Override
    protected List<TransferDTO> toDTOsList(Iterable<TransferModel> models) {
        Set<Integer> relationIds = new TreeSet<>();
        Set<Integer> blockchainIds = new TreeSet<>();
        for (TransferModel model : models) {
            relationIds.add(model.getMarketCurrencyRelationId());
            blockchainIds.add(model.getBlockchainId());
        }
        Map<Integer, RelationDTO> relations = relationService.getMap(relationIds);
        Map<Integer, BlockchainDTO> blockchains = blockchainsService.getMap(blockchainIds);

        List<TransferDTO> dtos = new LinkedList<>();
        for (TransferModel model : models) {
            TransferDTO transfer = modelToDTO(model, relations, blockchains);
            dtos.add(transfer);
        }
        return dtos;
    }

    @Override
    protected Map<Integer, TransferModel> toModelsMap(Iterable<TransferDTO> dtos) {
        Map<RelationDTO, RelationDTO> relations = relationService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));
        Map<BlockchainDTO, BlockchainDTO> blockchains = blockchainsService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        Map<Integer, TransferModel> models = new TreeMap<>();
        for (TransferDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            updateFields(dto, relations, blockchains);
            models.put(dto.getId(), new TransferModel(dto));
        }
        return models;
    }

    @Override
    protected List<TransferModel> toModelsList(Iterable<TransferDTO> dtos) {
        Map<RelationDTO, RelationDTO> relations = relationService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));
        Map<BlockchainDTO, BlockchainDTO> blockchains = blockchainsService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        List<TransferModel> models = new LinkedList<>();
        for (TransferDTO dto : dtos) {
            updateFields(dto, relations, blockchains);
            models.add(new TransferModel(dto));
        }
        return models;
    }

    @Override
    protected void addValidation(TransferDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null: " + dto);
        BlockchainDTO blockchain = dto.getBlockchain();
        if (blockchain == null || blockchain.getId() == null)
            throw new IllegalArgumentException("DTO blockchain and blockchain id should not be null: " + dto);
        RelationDTO relation = dto.getRelation();
        if (relation == null || relation.getId() == null)
            throw new IllegalArgumentException("DTO relation and relation id should not be null: " + dto);
    }

    @Override
    protected void updateValidation(TransferDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(TransferDTO dto) {
        updateValidation(dto);
    }

    public List<TransferDTO> getByFields(Iterable<TransferDTO> transfers) {
        return toDTOsList(dao.getByFields(toModelsList(transfers)));
    }

    private TransferDTO modelToDTO(
            TransferModel model,
            Map<Integer, RelationDTO> relations,
            Map<Integer, BlockchainDTO> blockchains
    ) {
        RelationDTO relation = relations.get(model.getMarketCurrencyRelationId());
        if (relation == null)
            throw new IllegalArgumentException("Relation not found for model=" + model);
        BlockchainDTO blockchain = blockchains.get(model.getBlockchainId());
        if (blockchain == null)
            throw new IllegalArgumentException("Blockchain not found for model=" + model);

        return new TransferDTO(model, relation, blockchain);
    }

    private void updateFields(
            TransferDTO transfer,
            Map<RelationDTO, RelationDTO> relations,
            Map<BlockchainDTO, BlockchainDTO> blockchains
    ) {
        RelationDTO relation = transfer.getRelation();
        if (relation.getId() == null)
            relation = relations.get(relation);
        if (relation == null)
            throw new IllegalArgumentException("Relation not found for transfer=" + transfer);

        BlockchainDTO blockchain = transfer.getBlockchain();
        if (blockchain.getId() == null)
            blockchain = blockchains.get(blockchain);
        if (blockchain == null)
            throw new IllegalArgumentException("Blockchain not found for transfer=" + transfer);

        transfer.setRelation(relation);
        transfer.setBlockchain(blockchain);
    }
}
