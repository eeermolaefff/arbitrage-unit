package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.TickerDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.models.TickerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TickersService extends ParentService<TickerModel, TickerDTO> {
    @Autowired
    public TickersService(
            TickerDAO tickerDAO,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(tickerDAO, batchSize);
    }

    @Override
    protected Map<Integer, TickerDTO> toDTOsMap(Iterable<TickerModel> models) {
        Map<Integer, TickerDTO>  dtos = new TreeMap<>();
        for (TickerModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.put(model.getId(), new TickerDTO(model));
        }
        return dtos;
    }

    @Override
    protected List<TickerDTO> toDTOsList(Iterable<TickerModel> models) {
        List<TickerDTO>  dtos = new LinkedList<>();
        for (TickerModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.add(new TickerDTO(model));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, TickerModel> toModelsMap(Iterable<TickerDTO> dtos) {
        Map<Integer, TickerModel>  models = new TreeMap<>();
        for (TickerDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            models.put(dto.getId(), new TickerModel(dto));
        }
        return models;
    }

    @Override
    protected List<TickerModel> toModelsList(Iterable<TickerDTO> dtos) {
        List<TickerModel>  models = new LinkedList<>();
        for (TickerDTO dto : dtos)
            models.add(new TickerModel(dto));
        return models;
    }

    @Override
    protected void addValidation(TickerDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getTicker() == null)
            throw new IllegalArgumentException("DTO ticker should not be null: " + dto);
    }

    @Override
    protected void updateValidation(TickerDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getId() == null || dto.getTicker() == null)
            throw new IllegalArgumentException("DTO id and ticker should not be null: " + dto);
    }

    @Override
    protected void removeValidation(TickerDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getId() == null)
            throw new IllegalArgumentException("TickeDTOrDTO id should not be null: " + dto);
    }
}
