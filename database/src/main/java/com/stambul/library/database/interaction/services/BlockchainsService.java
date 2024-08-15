package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.BlockchainDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.models.BlockchainModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class BlockchainsService extends ParentService<BlockchainModel, BlockchainDTO> {

    @Autowired
    public BlockchainsService(
            BlockchainDAO blockchainDAO,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(blockchainDAO, batchSize);
    }

    @Override
    protected Map<Integer, BlockchainDTO> toDTOsMap(Iterable<BlockchainModel> models) {
        Map<Integer, BlockchainDTO>  dtos = new TreeMap<>();
        for (BlockchainModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.put(model.getId(), new BlockchainDTO(model));
        }
        return dtos;
    }

    @Override
    protected List<BlockchainDTO> toDTOsList(Iterable<BlockchainModel> models) {
        List<BlockchainDTO>  dtos = new LinkedList<>();
        for (BlockchainModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.add(new BlockchainDTO(model));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, BlockchainModel> toModelsMap(Iterable<BlockchainDTO> dtos) {
        Map<Integer, BlockchainModel>  models = new TreeMap<>();
        for (BlockchainDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            models.put(dto.getId(), new BlockchainModel(dto));
        }
        return models;
    }

    @Override
    protected List<BlockchainModel> toModelsList(Iterable<BlockchainDTO> dtos) {
        List<BlockchainModel>  models = new LinkedList<>();
        for (BlockchainDTO dto : dtos)
            models.add(new BlockchainModel(dto));
        return models;
    }

    @Override
    protected void addValidation(BlockchainDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getId() == null || dto.getName() == null || dto.getBaseCoinId() == null)
            throw new IllegalArgumentException("DTO id, base coin id and name should not be null: " + dto);
    }

    @Override
    protected void updateValidation(BlockchainDTO dto) {
        addValidation(dto);
    }

    @Override
    protected void removeValidation(BlockchainDTO dto) {
        addValidation(dto);
    }
}
