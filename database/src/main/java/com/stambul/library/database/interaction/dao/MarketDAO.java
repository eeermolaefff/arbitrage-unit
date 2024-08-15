package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.MarketModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Component
public class MarketDAO extends ParentDAO<MarketModel> {

    @Autowired
    public MarketDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }    

    @Override
    protected String getTableName() {
        return "markets";
    };

    @Override
    protected BeanPropertyRowMapper<MarketModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(MarketModel.class);
    }

    @Override
    protected void getValidation(MarketModel model) {
        if (model == null)
            throw new IllegalArgumentException("Market should not be null");
        if (model.getSlug() == null) {
            String message = "Market full name and slug should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(MarketModel model) {
        getValidation(model);
        if (model.getFullName() == null) {
            String message = "Market full name should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(MarketModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Market id should not be null: " + model);
    }


    public List<MarketModel> getByFields(Iterable<MarketModel> models) {
        validate(models, this::getValidation);

        List<String> slugs = new LinkedList<>();
        for (MarketModel model : models)
            slugs.add(model.getSlug());

        return getByFields(new Pair<>(slugs, "slug"));
    }

    public void save(Iterable<MarketModel> models) {
        validate(models, this::saveValidation);

        List<MarketModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> ids = new LinkedList<>();
        List<String> slugs = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<Double> volumes = new LinkedList<>();
        List<Double> commissions = new LinkedList<>();
        List<Double> scores = new LinkedList<>();
        List<Double> traffics = new LinkedList<>();
        List<Integer> liquidity = new LinkedList<>();
        List<Integer> marketsNum = new LinkedList<>();
        List<Integer> coinsNum = new LinkedList<>();
        List<Timestamp> dates = new LinkedList<>();
        for (MarketModel model : models) {
            ids.add(model.getId());
            slugs.add(model.getSlug());
            names.add(model.getFullName());
            volumes.add(model.getDailyVolumeUsd());
            commissions.add(model.getSpotPercentCommission());
            scores.add(model.getScore());
            traffics.add(model.getTrafficScore());
            liquidity.add(model.getLiquidityScore());
            marketsNum.add(model.getNumberOfMarkets());
            coinsNum.add(model.getNumberOfCoins());
            dates.add(model.getDateLaunched());
        }

        saveByFields(
                new Pair<>(ids, "id"),
                new Pair<>(slugs, "slug"),
                new Pair<>(names, "full_name"),
                new Pair<>(volumes, "daily_volume_usd"),
                new Pair<>(commissions, "spot_percent_commission"),
                new Pair<>(scores, "score"),
                new Pair<>(traffics, "traffic_score"),
                new Pair<>(liquidity, "liquidity_score"),
                new Pair<>(marketsNum, "number_of_markets"),
                new Pair<>(coinsNum, "number_of_coins"),
                new Pair<>(dates, "date_launched")
        );
    }


    public void update(Iterable<MarketModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<String> slugs = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<Double> volumes = new LinkedList<>();
        List<Double> commissions = new LinkedList<>();
        List<Double> scores = new LinkedList<>();
        List<Double> traffics = new LinkedList<>();
        List<Integer> liquidity = new LinkedList<>();
        List<Integer> marketsNum = new LinkedList<>();
        List<Integer> coinsNum = new LinkedList<>();
        List<Timestamp> dates = new LinkedList<>();

        for (MarketModel model : models) {
            ids.add(model.getId());
            slugs.add(model.getSlug());
            names.add(model.getFullName());
            volumes.add(model.getDailyVolumeUsd());
            commissions.add(model.getSpotPercentCommission());
            scores.add(model.getScore());
            traffics.add(model.getTrafficScore());
            liquidity.add(model.getLiquidityScore());
            marketsNum.add(model.getNumberOfMarkets());
            coinsNum.add(model.getNumberOfCoins());
            dates.add(model.getDateLaunched());
        }

        List<Integer> foundIds = getById(ids).stream().map(MarketModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(slugs, "slug"),
                new Pair<>(names, "full_name"),
                new Pair<>(volumes, "daily_volume_usd"),
                new Pair<>(commissions, "spot_percent_commission"),
                new Pair<>(scores, "score"),
                new Pair<>(traffics, "traffic_score"),
                new Pair<>(liquidity, "liquidity_score"),
                new Pair<>(marketsNum, "number_of_markets"),
                new Pair<>(coinsNum, "number_of_coins"),
                new Pair<>(dates, "date_launched"),
                new Pair<>(ids, "id")
        );
    }
}
