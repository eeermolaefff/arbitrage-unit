package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.CurrencyDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.database.objects.models.CurrencyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class CurrenciesService extends ParentService<CurrencyModel, CurrencyDTO> {
    private final RelationTimestampsService relationTimestampsService;
    private final ContractTimestampsService contractTimestampService;

    @Autowired
    public CurrenciesService(
            CurrencyDAO currencyDAO,
            RelationTimestampsService relationTimestampsService,
            ContractTimestampsService contractTimestampService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(currencyDAO, batchSize);
        this.relationTimestampsService = relationTimestampsService;
        this.contractTimestampService = contractTimestampService;
    }

    @Override
    protected List<CurrencyModel> toModelsList(Iterable<CurrencyDTO> dtos) {
        List<CurrencyModel> models = new LinkedList<>();
        for (CurrencyDTO dto : dtos)
            models.add(new CurrencyModel(dto));
        return models;
    }

    @Override
    protected Map<Integer, CurrencyModel> toModelsMap(Iterable<CurrencyDTO> dtos) {
        Map<Integer, CurrencyModel> models = new TreeMap<>();
        for (CurrencyDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);
            models.put(dto.getId(), new CurrencyModel(dto));
        }
        return models;
    }

    @Override
    protected List<CurrencyDTO> toDTOsList(Iterable<CurrencyModel> models) {
        Set<Integer> currencyIds = StreamSupport.stream(models.spliterator(), false)
                .map(CurrencyModel::getId).collect(Collectors.toSet());

        Map<Integer, TimestampDTO> relations = relationTimestampsService.getByCurrencyIds(currencyIds).stream()
                .collect(Collectors.toMap(TimestampDTO::getParentId, Function.identity()));
        Map<Integer, TimestampDTO> contracts = contractTimestampService.getByCurrencyIds(currencyIds).stream()
                .collect(Collectors.toMap(TimestampDTO::getParentId, Function.identity()));

        List<CurrencyDTO> dtos = new LinkedList<>();
        for (CurrencyModel model : models) {
            CurrencyDTO dto = new CurrencyDTO(model, contracts.get(model.getId()), relations.get(model.getId()));
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    protected Map<Integer, CurrencyDTO> toDTOsMap(Iterable<CurrencyModel> models) {
        Set<Integer> currencyIds = StreamSupport.stream(models.spliterator(), false)
                .map(CurrencyModel::getId).collect(Collectors.toSet());

        Map<Integer, TimestampDTO> relations = relationTimestampsService.getByCurrencyIds(currencyIds).stream()
                .collect(Collectors.toMap(TimestampDTO::getParentId, Function.identity()));
        Map<Integer, TimestampDTO> contracts = contractTimestampService.getByCurrencyIds(currencyIds).stream()
                .collect(Collectors.toMap(TimestampDTO::getParentId, Function.identity()));

        Map<Integer, CurrencyDTO> dtos = new TreeMap<>();
        for (CurrencyModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);
            CurrencyDTO dto = new CurrencyDTO(model, contracts.get(model.getId()), relations.get(model.getId()));
            dtos.put(model.getId(), dto);
        }
        return dtos;
    }

    @Override
    protected void addValidation(CurrencyDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("Currency should not be null");
        if (dto.getFullName() == null || dto.getId() == null || dto.getSlug() == null) {
            String message = "Currency full name, coinmarketcap id and slug should not be null: " + dto;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(CurrencyDTO dto) {
        addValidation(dto);
    }

    @Override
    protected void removeValidation(CurrencyDTO dto) {
        addValidation(dto);
    }
}
