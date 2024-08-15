package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.TradingPairModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class TradingPairDAO extends ParentDAO<TradingPairModel> {
    @Autowired
    public TradingPairDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "trading_pairs";
    };

    @Override
    protected BeanPropertyRowMapper<TradingPairModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(TradingPairModel.class);
    }

    @Override
    protected void getValidation(TradingPairModel model) {
        if (model == null)
            throw new IllegalArgumentException("Trading pair should not be null");
        if (model.getMarketId() == null || model.getBaseAssetId() == null || model.getQuoteAssetId() == null) {
            String message = "Trading pair market, base and quote asset ids should not be null: pair=" + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(TradingPairModel model) {
        getValidation(model);
        if (model.getTickerId() == null)
            throw new IllegalArgumentException("Trading pair ticker id should not be null: pair=" + model);
    }

    @Override
    protected void updateValidation(TradingPairModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Trading pair id should not be null: " + model);
    }

    public List<TradingPairModel> getByFields(Iterable<TradingPairModel> models) {
        validate(models, this::getValidation);

        List<Integer> marketIds = new LinkedList<>();
        List<Integer> baseIds = new LinkedList<>();
        List<Integer> quoteIds = new LinkedList<>();
        for (TradingPairModel pair : models) {
            marketIds.add(pair.getMarketId());
            baseIds.add(pair.getBaseAssetId());
            quoteIds.add(pair.getQuoteAssetId());
        }

        return getByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(baseIds, "base_asset_id"),
                new Pair<>(quoteIds, "quote_asset_id")
        );
    }

    public void save(Iterable<TradingPairModel> models) {
        validate(models, this::saveValidation);

        List<TradingPairModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> marketIds = new LinkedList<>();
        List<Integer> baseIds = new LinkedList<>();
        List<Integer> quoteIds = new LinkedList<>();
        List<Integer> tickerIds = new LinkedList<>();

        for (TradingPairModel model : models) {
            marketIds.add(model.getMarketId());
            baseIds.add(model.getBaseAssetId());
            quoteIds.add(model.getQuoteAssetId());
            tickerIds.add(model.getTickerId());
        }

        saveByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(baseIds, "base_asset_id"),
                new Pair<>(quoteIds, "quote_asset_id"),
                new Pair<>(tickerIds, "ticker_id")
        );
    }

    public void update(Iterable<TradingPairModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> marketIds = new LinkedList<>();
        List<Integer> baseIds = new LinkedList<>();
        List<Integer> quoteIds = new LinkedList<>();
        List<Integer> tickerIds = new LinkedList<>();
        for (TradingPairModel model : models) {
            ids.add(model.getId());
            marketIds.add(model.getMarketId());
            baseIds.add(model.getBaseAssetId());
            quoteIds.add(model.getQuoteAssetId());
            tickerIds.add(model.getTickerId());
        }


        List<Integer> foundIds = getById(ids).stream().map(TradingPairModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(baseIds, "base_asset_id"),
                new Pair<>(quoteIds, "quote_asset_id"),
                new Pair<>(tickerIds, "ticker_id"),
                new Pair<>(ids, "id")
        );
    }

    public List<TradingPairModel> getByMarketIds(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "market_id"));
    }
}
