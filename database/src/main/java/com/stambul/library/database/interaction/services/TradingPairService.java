package com.stambul.library.database.interaction.services;

import com.stambul.library.database.interaction.dao.TradingPairDAO;
import com.stambul.library.database.interaction.services.interfaces.ParentService;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.models.TradingPairModel;
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
public class TradingPairService extends ParentService<TradingPairModel, TradingPairDTO> {
    private final TickersService tickersService;

    @Autowired
    public TradingPairService(
            TradingPairDAO dao,
            TickersService tickersService,
            @Value("${database.batch.size}") int batchSize
    ) {
        super(dao, batchSize);
        this.tickersService = tickersService;
    }

    @Override
    protected Map<Integer, TradingPairDTO> toDTOsMap(Iterable<TradingPairModel> models) {
        List<Integer> tickersIds = StreamSupport.stream(models.spliterator(), false)
                .map(TradingPairModel::getTickerId).toList();
        Map<Integer, TickerDTO> tickers = tickersService.getMap(tickersIds);

        Map<Integer, TradingPairDTO>  dtos = new TreeMap<>();
        for (TradingPairModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TickerDTO ticker = tickers.get(model.getTickerId());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for model=" + model);
            dtos.put(model.getId(), new TradingPairDTO(model, ticker));
        }
        return dtos;
    }

    @Override
    protected List<TradingPairDTO> toDTOsList(Iterable<TradingPairModel> models) {
        List<Integer> tickersIds = StreamSupport.stream(models.spliterator(), false)
                .map(TradingPairModel::getTickerId).toList();
        Map<Integer, TickerDTO> tickers = tickersService.getMap(tickersIds);

        List<TradingPairDTO>  dtos = new LinkedList<>();
        for (TradingPairModel model : models) {
            if (model.getId() == null)
                throw new IllegalArgumentException("Null model.getId() value while adding to map: " + model);

            TickerDTO ticker = tickers.get(model.getTickerId());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for model=" + model);
            dtos.add(new TradingPairDTO(model, ticker));
        }
        return dtos;
    }

    @Override
    protected Map<Integer, TradingPairModel> toModelsMap(Iterable<TradingPairDTO> dtos) {
        Map<String, TickerDTO> tickers = tickersService.getAll().stream()
                .collect(Collectors.toMap(TickerDTO::getTicker, Function.identity()));

        Map<Integer, TradingPairModel>  models = new TreeMap<>();
        for (TradingPairDTO dto : dtos) {
            if (dto.getId() == null)
                throw new IllegalArgumentException("Null dto.getId() value while adding to map: " + dto);

            TickerDTO ticker = dto.getTicker();
            if (ticker.getId() == null)
                ticker = tickers.get(dto.getTicker().getTicker());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for dto=" + dto);

            models.put(dto.getId(), new TradingPairModel(dto, ticker));
        }
        return models;
    }

    @Override
    protected List<TradingPairModel> toModelsList(Iterable<TradingPairDTO> dtos) {
        Map<String, TickerDTO> tickers = tickersService.getAll().stream()
                .collect(Collectors.toMap(TickerDTO::getTicker, Function.identity()));

        List<TradingPairModel>  models = new LinkedList<>();
        for (TradingPairDTO dto : dtos) {
            TickerDTO ticker = dto.getTicker();
            if (ticker.getId() == null)
                ticker = tickers.get(dto.getTicker().getTicker());
            if (ticker == null)
                throw new IllegalArgumentException("Ticker not found for dto=" + dto);

            models.add(new TradingPairModel(dto, ticker));
        }
        return models;
    }

    @Override
    protected void addValidation(TradingPairDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO should not be null: " + dto);
        if (dto.getMarketId() == null || dto.getBaseAssetId() == null || dto.getQuoteAssetId() == null)
            throw new IllegalArgumentException("DTO market, base and quote ids should not be null: " + dto);
        if (dto.getTicker() == null)
            throw new IllegalArgumentException("DTO ticker should not be null: " + dto);
    }

    @Override
    protected void updateValidation(TradingPairDTO dto) {
        addValidation(dto);
        if (dto.getId() == null)
            throw new IllegalArgumentException("DTO id should not be null: " + dto);
    }

    @Override
    protected void removeValidation(TradingPairDTO dto) {
        updateValidation(dto);
    }

    @Override
    protected void beforeAddingActions(Iterable<TradingPairDTO> dtos) {
        List<TickerDTO> tickersList = new LinkedList<>();
        for (TradingPairDTO dto : dtos) {
            String ticker = dto.getTicker().getTicker();
            tickersList.add(new TickerDTO(ticker));
        }
        tickersService.addNonExistingToDatabase(tickersList);
    }

    @Override
    protected void beforeUpdatingActions(Iterable<TradingPairDTO> dtos) {
        beforeAddingActions(dtos);
    }

    public List<TradingPairDTO> getByMarketId(int id) {
        return getByMarketIds(List.of(id));
    }


    public List<TradingPairDTO> getByMarketIds(Iterable<Integer> ids) {
        if (IterableTools.size(ids) == 0)
            return new LinkedList<>();
        validate(ids, this::fieldValidation);

        return toDTOsList(((TradingPairDAO) dao).getByMarketIds(ids));
    }
}
