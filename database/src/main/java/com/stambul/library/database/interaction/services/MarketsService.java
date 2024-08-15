package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.MarketDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.library.database.objects.models.MarketModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class MarketsService extends ParentService<MarketModel, MarketDTO> {
    @Autowired
    public MarketsService(
            MarketDAO marketDAO,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(marketDAO, batchSize);
    }

    @Override
    protected List<MarketModel> toModelsList(Iterable<MarketDTO> dtos) {
        List<MarketModel> models = new LinkedList<>();
        for (MarketDTO model : dtos)
            models.add(new MarketModel(model));
        return models;
    }

    @Override
    protected Map<Integer, MarketModel> toModelsMap(Iterable<MarketDTO> dtos) {
        Map<Integer, MarketModel> models = new TreeMap<>();
        for (MarketDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            models.put(dto.getId(), new MarketModel(dto));
        }
        return models;
    }

    @Override
    protected List<MarketDTO> toDTOsList(Iterable<MarketModel> models) {
        List<MarketDTO>  dtos = new LinkedList<>();
        for (MarketModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.add(new MarketDTO(model));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, MarketDTO> toDTOsMap(Iterable<MarketModel> models) {
        Map<Integer, MarketDTO> dtos = new TreeMap<>();
        for (MarketModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.put(model.getId(), new MarketDTO(model));
        }
        return dtos;
    }

    @Override
    protected void addValidation(MarketDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getId() == null || dto.getSlug() == null || dto.getFullName() == null)
            throw new IllegalArgumentException("DTO id, slug and full name should not be null: " + dto);
    }

    @Override
    protected void updateValidation(MarketDTO dto) {
        addValidation(dto);
    }

    @Override
    protected void removeValidation(MarketDTO dto) {
        addValidation(dto);
    }
}
