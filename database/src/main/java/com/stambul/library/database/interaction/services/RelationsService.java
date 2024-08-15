package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.RelationDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.models.RelationModel;
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
public class RelationsService extends ParentService<RelationModel, RelationDTO> {
    private final TickersService tickersService;

    @Autowired
    public RelationsService(
            RelationDAO relationDAO,
            TickersService tickersService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(relationDAO, batchSize);
        this.tickersService = tickersService;
    }

    @Override
    protected Map<Integer, RelationDTO> toDTOsMap(Iterable<RelationModel> models) {
        List<Integer> tickersIds = StreamSupport.stream(models.spliterator(), false)
                .map(RelationModel::getCurrencyTickerId).toList();
        Map<Integer, TickerDTO> tickers = tickersService.getMap(tickersIds);

        Map<Integer, RelationDTO>  dtos = new TreeMap<>();
        for (RelationModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TickerDTO ticker = tickers.get(model.getCurrencyTickerId());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for model=" + model);
            dtos.put(model.getId(), new RelationDTO(model, ticker));
        }
        return dtos;
    }

    @Override
    protected List<RelationDTO> toDTOsList(Iterable<RelationModel> models) {
        List<Integer> tickersIds = StreamSupport.stream(models.spliterator(), false)
                .map(RelationModel::getCurrencyTickerId).toList();
        Map<Integer, TickerDTO> tickers = tickersService.getMap(tickersIds);

        List<RelationDTO>  dtos = new LinkedList<>();
        for (RelationModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TickerDTO ticker = tickers.get(model.getCurrencyTickerId());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for model=" + model);
            dtos.add(new RelationDTO(model, ticker));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, RelationModel> toModelsMap(Iterable<RelationDTO> dtos) {
        Map<String, TickerDTO> tickers = tickersService.getAll().stream()
                .collect(Collectors.toMap(TickerDTO::getTicker, Function.identity()));

        Map<Integer, RelationModel>  models = new TreeMap<>();
        for (RelationDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);

            TickerDTO ticker = dto.getTicker();
            if (ticker.getId() == null)
                ticker = tickers.get(dto.getTicker().getTicker());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for dto=" + dto);

            models.put(dto.getId(), new RelationModel(dto, ticker));
        }
        return models;
    }

    @Override
    protected List<RelationModel> toModelsList(Iterable<RelationDTO> dtos) {
        Map<String, TickerDTO> tickers = tickersService.getAll().stream()
                .collect(Collectors.toMap(TickerDTO::getTicker, Function.identity()));

        List<RelationModel>  models = new LinkedList<>();
        for (RelationDTO dto : dtos) {
            TickerDTO ticker = dto.getTicker();
            if (ticker.getId() == null)
                ticker = tickers.get(dto.getTicker().getTicker());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for dto=" + dto);

            models.add(new RelationModel(dto, ticker));
        }
        return models;
    }

    @Override
    protected void addValidation(RelationDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null: " + dto);
        if (dto.getCurrencyId() == null || dto.getMarketId() == null || dto.getTicker() == null)
            throw new IllegalArgumentException("DTO ticker, currency and market id should not be null: " + dto);
        if (dto.getExchangeType() == null || dto.getExchangeCategory() == null)
            throw new IllegalArgumentException("DTO exchange type and exchange category should not be null: " + dto);
    }

    @Override
    protected void updateValidation(RelationDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(RelationDTO dto) {
        updateValidation(dto);
    }

    @Override
    protected void beforeAddingActions(Iterable<RelationDTO> relations) {
        List<TickerDTO> tickersList = new LinkedList<>();
        for (RelationDTO relation : relations) {
            String ticker = relation.getTicker().getTicker();
            tickersList.add(new TickerDTO(ticker));
        }
        tickersService.addNonExistingToDatabase(tickersList);
    }

    @Override
    protected void beforeUpdatingActions(Iterable<RelationDTO> relations) {
        beforeAddingActions(relations);
    }


    public List<RelationDTO> getByCurrencyIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((RelationDAO) dao).getByCurrencyId(ids));
    }

    public List<RelationDTO> getByMarketIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((RelationDAO) dao).getByMarketId(ids));
    }
}
