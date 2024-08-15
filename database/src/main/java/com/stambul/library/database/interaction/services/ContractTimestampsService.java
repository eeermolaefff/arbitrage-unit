package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.ContractTimestampDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.database.objects.models.TimestampModel;
import com.stambul.library.tools.IterableTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class ContractTimestampsService extends ParentService<TimestampModel, TimestampDTO> {

    @Autowired
    public ContractTimestampsService(
            ContractTimestampDAO contractTimestampDAO,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(contractTimestampDAO, batchSize);
    }

    public List<TimestampDTO> getByCurrencyIds(Iterable<Integer> ids) {
        int size = IterableTools.size(ids);
        if (size == 0)
            return new LinkedList<>();
        if (size > batchSize)
            return getByCurrencyIds(ids, batchSize);

        validate(ids, this::fieldValidation);
        return toDTOsList(((ContractTimestampDAO) dao).getByCurrencyId(ids));
    }

    protected List<TimestampDTO> getByCurrencyIds(Iterable<Integer> ids, int batchSize) {
        String formatMessage = "%d/%d objects has extracted from database";
        ListHandler handler = new ListHandler();
        batchAction(ids, batchSize, handler, formatMessage);
        return handler.getList();
    }

    protected class ListHandler implements Handler<Iterable<Integer>> {
        private final List<TimestampDTO> list = new LinkedList<>();
        @Override
        public void handle(Iterable<Integer> ids) {
            this.list.addAll(ContractTimestampsService.this.getByCurrencyIds(ids));
        }
        public List<TimestampDTO> getList() {
            return list;
        }
    }

    @Override
    protected Map<Integer, TimestampDTO> toDTOsMap(Iterable<TimestampModel> models) {
        Map<Integer, TimestampDTO>  dtos = new TreeMap<>();
        for (TimestampModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.put(model.getId(), new TimestampDTO(model));
        }
        return dtos;
    }

    @Override
    protected List<TimestampDTO> toDTOsList(Iterable<TimestampModel> models) {
        List<TimestampDTO>  dtos = new LinkedList<>();
        for (TimestampModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            dtos.add(new TimestampDTO(model));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, TimestampModel> toModelsMap(Iterable<TimestampDTO> dtos) {
        Map<Integer, TimestampModel>  models = new TreeMap<>();
        for (TimestampDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            models.put(dto.getId(), new TimestampModel(dto));
        }
        return models;
    }

    @Override
    protected List<TimestampModel> toModelsList(Iterable<TimestampDTO> dtos) {
        List<TimestampModel>  models = new LinkedList<>();
        for (TimestampDTO dto : dtos)
            models.add(new TimestampModel(dto));
        return models;
    }

    @Override
    protected void addValidation(TimestampDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null");
        if (dto.getParentId() == null)
            throw new IllegalArgumentException("DTO parent id should not be null: " + dto);
    }

    @Override
    protected void updateValidation(TimestampDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null");
    }

    @Override
    protected void removeValidation(TimestampDTO dto) {
        updateValidation(dto);
    }
}
