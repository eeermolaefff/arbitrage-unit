package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.OrderbookDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.models.OrderbookModel;
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
public class OrderbookService extends ParentService<OrderbookModel, OrderbookDTO> {
    private final TradingPairService tradingPairService;

    @Autowired
    public OrderbookService(
            OrderbookDAO dao,
            TradingPairService tradingPairService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(dao, batchSize);
        this.tradingPairService = tradingPairService;
    }

    @Override
    protected Map<Integer, OrderbookDTO> toDTOsMap(Iterable<OrderbookModel> models) {
        List<Integer> pairIds = StreamSupport.stream(models.spliterator(), false)
                .map(OrderbookModel::getTradingPairId).toList();
        Map<Integer, TradingPairDTO> pairs = tradingPairService.getMap(pairIds);

        Map<Integer, OrderbookDTO>  dtos = new TreeMap<>();
        for (OrderbookModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TradingPairDTO pair = pairs.get(model.getTradingPairId());
            if (pair == null)
                throw new IllegalArgumentException("Trading pair not found for model=" + model);
            dtos.put(model.getId(), new OrderbookDTO(model, pair));
        }
        return dtos;
    }

    @Override
    protected List<OrderbookDTO> toDTOsList(Iterable<OrderbookModel> models) {
        List<Integer> pairIds = StreamSupport.stream(models.spliterator(), false)
                .map(OrderbookModel::getTradingPairId).toList();
        Map<Integer, TradingPairDTO> pairs = tradingPairService.getMap(pairIds);

        List<OrderbookDTO>  dtos = new LinkedList<>();
        for (OrderbookModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TradingPairDTO pair = pairs.get(model.getTradingPairId());
            if (pair == null)
                throw new IllegalArgumentException("Trading pair not found for model=" + model);
            dtos.add(new OrderbookDTO(model, pair));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, OrderbookModel> toModelsMap(Iterable<OrderbookDTO> dtos) {
        Map<TradingPairDTO, TradingPairDTO> pairs = tradingPairService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        Map<Integer, OrderbookModel>  models = new TreeMap<>();
        for (OrderbookDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);

            TradingPairDTO pair = dto.getTradingPair();
            if (pair.getId() == null)
                pair = pairs.get(pair);
            if (pair == null)
                throw new IllegalArgumentException("Trading pair not found for dto=" + dto);

            models.put(dto.getId(), new OrderbookModel(dto, pair));
        }
        return models;
    }

    @Override
    protected List<OrderbookModel> toModelsList(Iterable<OrderbookDTO> dtos) {
        Map<TradingPairDTO, TradingPairDTO> pairs = tradingPairService.getAll().stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity(), (p1, p2) -> p1, TreeMap::new));

        List<OrderbookModel>  models = new LinkedList<>();
        for (OrderbookDTO dto : dtos) {
            TradingPairDTO pair = dto.getTradingPair();
            if (pair.getId() == null)
                pair = pairs.get(pair);
            if (pair == null)
                throw new IllegalArgumentException("Trading pair not found for dto=" + dto);

            models.add(new OrderbookModel(dto, pair));
        }
        return models;
    }

    @Override
    protected void addValidation(OrderbookDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null: " + dto);
        TradingPairDTO pair = dto.getTradingPair();
        if (pair == null)
            throw new IllegalArgumentException("DTO trading pair should not be null: " + dto);
        if (pair.getMarketId() == null || pair.getTicker() == null || pair.getBaseAssetId() == null || pair.getQuoteAssetId() == null)
            throw new IllegalArgumentException("DTO market, base, quote ids and ticker should not be null: " + dto);
    }

    @Override
    protected void updateValidation(OrderbookDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(OrderbookDTO dto) {
        updateValidation(dto);
    }

    @Override
    protected void beforeAddingActions(Iterable<OrderbookDTO> dtos) {
        List<TradingPairDTO> pairsList = new LinkedList<>();
        for (OrderbookDTO dto : dtos)
            pairsList.add(dto.getTradingPair());
        tradingPairService.addNonExistingToDatabase(pairsList);
    }

    @Override
    protected void beforeUpdatingActions(Iterable<OrderbookDTO> dtos) {
        beforeAddingActions(dtos);
    }

    public List<OrderbookDTO> getByTradingPairIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((OrderbookDAO) dao).getByTradingPairsIds(ids));
    }
}
