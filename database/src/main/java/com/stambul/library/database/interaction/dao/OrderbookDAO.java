package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.OrderbookModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class OrderbookDAO extends ParentDAO<OrderbookModel> {
    @Autowired
    public OrderbookDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "orderbooks";
    };

    @Override
    protected BeanPropertyRowMapper<OrderbookModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(OrderbookModel.class);
    }

    @Override
    protected void getValidation(OrderbookModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model should not be null");
        if (model.getTradingPairId() == null) {
            String message = "Model trading pair id should not be null: model=" + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(OrderbookModel model) {
        getValidation(model);
        if (model.getTradingType() == null) {
            String message = "Model trading type should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(OrderbookModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Model id should not be null: " + model);
    }

    public List<OrderbookModel> getByFields(Iterable<OrderbookModel> models) {
        validate(models, this::getValidation);

        List<Integer> tradingPairIds = new LinkedList<>();
        for (OrderbookModel model : models) {
            tradingPairIds.add(model.getTradingPairId());
        }

        return getByFields(
                new Pair<>(tradingPairIds, "trading_pair_id")
        );
    }

    public void save(Iterable<OrderbookModel> models) {
        validate(models, this::saveValidation);

        List<OrderbookModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> tradingPairIds = new LinkedList<>();
        List<Boolean> isActive = new LinkedList<>();
        List<Double> bigPrices = new LinkedList<>();
        List<Double> askPrices = new LinkedList<>();
        List<Double> bidQty = new LinkedList<>();
        List<Double> askQty = new LinkedList<>();
        List<Double> dailyUsd = new LinkedList<>();
        List<Double> dailyBase = new LinkedList<>();
        List<Double> dailyQuote = new LinkedList<>();
        List<Boolean> isSpotAllowed = new LinkedList<>();
        List<Boolean> isMarginAllowed = new LinkedList<>();
        List<String> tradingTypes = new LinkedList<>();
        for (OrderbookModel pair : models) {
            tradingPairIds.add(pair.getTradingPairId());
            isActive.add(pair.getIsActive());
            bigPrices.add(pair.getBidPrice());
            askPrices.add(pair.getAskPrice());
            bidQty.add(pair.getBidQty());
            askQty.add(pair.getAskQty());
            dailyUsd.add(pair.getDailyVolumeUsd());
            dailyBase.add(pair.getDailyVolumeBase());
            dailyQuote.add(pair.getDailyVolumeQuote());
            isSpotAllowed.add(pair.getIsSpotTradingAllowed());
            isMarginAllowed.add(pair.getIsMarginTradingAllowed());
            tradingTypes.add(pair.getTradingType());
        }

        saveByFields(
                new Pair<>(tradingPairIds, "trading_pair_id"),
                new Pair<>(isActive, "is_active"),
                new Pair<>(bigPrices, "bid_price"),
                new Pair<>(askPrices, "ask_price"),
                new Pair<>(bidQty, "bid_qty"),
                new Pair<>(askQty, "ask_qty"),
                new Pair<>(dailyUsd, "daily_volume_usd"),
                new Pair<>(dailyBase, "daily_volume_base"),
                new Pair<>(dailyQuote, "daily_volume_quote"),
                new Pair<>(isSpotAllowed, "is_spot_trading_allowed"),
                new Pair<>(isMarginAllowed, "is_margin_trading_allowed"),
                new Pair<>(tradingTypes, "trading_type")
        );
    }

    public void update(Iterable<OrderbookModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> tradingPairIds = new LinkedList<>();
        List<Boolean> isActive = new LinkedList<>();
        List<Double> bigPrices = new LinkedList<>();
        List<Double> askPrices = new LinkedList<>();
        List<Double> bidQty = new LinkedList<>();
        List<Double> askQty = new LinkedList<>();
        List<Double> dailyUsd = new LinkedList<>();
        List<Double> dailyBase = new LinkedList<>();
        List<Double> dailyQuote = new LinkedList<>();
        List<Boolean> isSpotAllowed = new LinkedList<>();
        List<Boolean> isMarginAllowed = new LinkedList<>();
        List<String> tradingTypes = new LinkedList<>();
        for (OrderbookModel model : models) {
            ids.add(model.getId());
            tradingPairIds.add(model.getTradingPairId());
            isActive.add(model.getIsActive());
            bigPrices.add(model.getBidPrice());
            askPrices.add(model.getAskPrice());
            bidQty.add(model.getBidQty());
            askQty.add(model.getAskQty());
            dailyUsd.add(model.getDailyVolumeUsd());
            dailyBase.add(model.getDailyVolumeBase());
            dailyQuote.add(model.getDailyVolumeQuote());
            isSpotAllowed.add(model.getIsSpotTradingAllowed());
            isMarginAllowed.add(model.getIsMarginTradingAllowed());
            tradingTypes.add(model.getTradingType());
        }


        List<Integer> foundIds = getById(ids).stream().map(OrderbookModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(tradingPairIds, "trading_pair_id"),
                new Pair<>(isActive, "is_active"),
                new Pair<>(bigPrices, "bid_price"),
                new Pair<>(askPrices, "ask_price"),
                new Pair<>(bidQty, "bid_qty"),
                new Pair<>(askQty, "ask_qty"),
                new Pair<>(dailyUsd, "daily_volume_usd"),
                new Pair<>(dailyBase, "daily_volume_base"),
                new Pair<>(dailyQuote, "daily_volume_quote"),
                new Pair<>(isSpotAllowed, "is_spot_trading_allowed"),
                new Pair<>(isMarginAllowed, "is_margin_trading_allowed"),
                new Pair<>(tradingTypes, "trading_type"),
                new Pair<>(ids, "id")
        );
    }

    public List<OrderbookModel> getByTradingPairsIds(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "trading_pair_id"));
    }
}
