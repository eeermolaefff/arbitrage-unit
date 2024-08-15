package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.RelationModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class RelationDAO extends ParentDAO<RelationModel> {

    @Autowired
    public RelationDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "market_currency_relations";
    };

    @Override
    protected BeanPropertyRowMapper<RelationModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(RelationModel.class);
    }

    @Override
    protected void getValidation(RelationModel model) {
        if (model == null)
            throw new IllegalArgumentException("Market-currency relation should not be null");
        if (model.getMarketId() == null || model.getCurrencyId() == null) {
            String message = "Market-currency relation market and currency ids should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
        if (model.getExchangeCategory() == null || model.getExchangeType() == null) {
            String message = "Market-currency relation exchange type and exchange category should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(RelationModel model) {
        getValidation(model);
        if (model.getCurrencyTickerId() == null) {
            String message = "Market-currency relation ticker id should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(RelationModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Market-currency relation id should not be null: " + model);
    }

    public List<RelationModel> getByCurrencyId(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "currency_id"));
    }

    public List<RelationModel> getByMarketId(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "market_id"));
    }

    public List<RelationModel> getByFields(Iterable<RelationModel> models) {
        validate(models, this::getValidation);

        List<Integer> marketIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        List<String> types = new LinkedList<>();
        List<String> categories = new LinkedList<>();
        for (RelationModel model : models) {
            marketIds.add(model.getMarketId());
            currencyIds.add(model.getCurrencyId());
            types.add(model.getExchangeType());
            categories.add(model.getExchangeCategory());
        }

        return getByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(currencyIds, "currency_id"),
                new Pair<>(types, "exchange_type"),
                new Pair<>(categories, "exchange_category")
        );
    }

    public void save(Iterable<RelationModel> models) {
        validate(models, this::saveValidation);

        List<RelationModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> marketIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        List<Integer> currencyTickerIds = new LinkedList<>();
        List<String> exchangeTypes = new LinkedList<>();
        List<String> exchangeCategories = new LinkedList<>();
        for (RelationModel model : models) {
            marketIds.add(model.getMarketId());
            currencyIds.add(model.getCurrencyId());
            currencyTickerIds.add(model.getCurrencyTickerId());
            exchangeTypes.add(model.getExchangeType());
            exchangeCategories.add(model.getExchangeCategory());
        }

        saveByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(currencyIds, "currency_id"),
                new Pair<>(currencyTickerIds, "currency_ticker_id"),
                new Pair<>(exchangeTypes, "exchange_type"),
                new Pair<>(exchangeCategories, "exchange_category")
        );
    }

    public void update(Iterable<RelationModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> marketIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        List<Integer> currencyTickerIds = new LinkedList<>();
        List<String> exchangeTypes = new LinkedList<>();
        List<String> exchangeCategories = new LinkedList<>();
        for (RelationModel model : models) {
            ids.add(model.getId());
            marketIds.add(model.getMarketId());
            currencyIds.add(model.getCurrencyId());
            currencyTickerIds.add(model.getCurrencyTickerId());
            exchangeTypes.add(model.getExchangeType());
            exchangeCategories.add(model.getExchangeCategory());
        }

        List<Integer> foundIds = getById(ids).stream().map(RelationModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(marketIds, "market_id"),
                new Pair<>(currencyIds, "currency_id"),
                new Pair<>(currencyTickerIds, "currency_ticker_id"),
                new Pair<>(exchangeTypes, "exchange_type"),
                new Pair<>(exchangeCategories, "exchange_category"),
                new Pair<>(ids, "id")
        );

    }
}
