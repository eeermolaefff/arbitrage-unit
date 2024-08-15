package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.SwapModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class SwapDAO extends ParentDAO<SwapModel> {
    @Autowired
    public SwapDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "swaps";
    };

    @Override
    protected BeanPropertyRowMapper<SwapModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(SwapModel.class);
    }

    @Override
    protected void getValidation(SwapModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model should not be null");
        if (model.getTradingPairId() == null) {
            String message = "Model trading pair id should not be null: model=" + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(SwapModel model) {
        getValidation(model);
        if (model.getTradingType() == null) {
            String message = "Model trading type should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(SwapModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Model id should not be null: " + model);
    }

    public List<SwapModel> getByFields(Iterable<SwapModel> models) {
        validate(models, this::getValidation);

        List<Integer> tradingPairIds = new LinkedList<>();
        List<Integer> baseContractIds = new LinkedList<>();
        List<Integer> quoteContractIds = new LinkedList<>();
        List<Double> feesPercentage = new LinkedList<>();
        for (SwapModel model : models) {
            tradingPairIds.add(model.getTradingPairId());
            baseContractIds.add(model.getQuoteContractId());
            quoteContractIds.add(model.getBaseContractId());
            feesPercentage.add(model.getFeePercentage());
        }

        return getByFields(
                new Pair<>(tradingPairIds, "trading_pair_id"),
                new Pair<>(baseContractIds, "base_contract_id"),
                new Pair<>(quoteContractIds, "quote_contract_id"),
                new Pair<>(feesPercentage, "fee_percentage")
        );
    }

    public void save(Iterable<SwapModel> models) {
        validate(models, this::saveValidation);

        List<SwapModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> tradingPairIds = new LinkedList<>();
        List<Integer> baseContractIds = new LinkedList<>();
        List<Integer> quoteContractIds = new LinkedList<>();
        List<Boolean> isActive = new LinkedList<>();
        List<Double> basePrices = new LinkedList<>();
        List<Double> quotePrices = new LinkedList<>();
        List<Double> fees = new LinkedList<>();
        List<Double> liquidity = new LinkedList<>();
        List<String> hashes = new LinkedList<>();
        List<Double> dailyUsd = new LinkedList<>();
        List<Double> dailyBase = new LinkedList<>();
        List<Double> dailyQuote = new LinkedList<>();
        List<Double> tvlUsd = new LinkedList<>();
        List<Double> tvlBase = new LinkedList<>();
        List<Double> tvlQuote = new LinkedList<>();
        List<String> tradingTypes = new LinkedList<>();
        for (SwapModel model : models) {
            tradingPairIds.add(model.getTradingPairId());
            baseContractIds.add(model.getBaseContractId());
            quoteContractIds.add(model.getQuoteContractId());
            isActive.add(model.getIsActive());
            basePrices.add(model.getBasePrice());
            quotePrices.add(model.getQuotePrice());
            fees.add(model.getFeePercentage());
            liquidity.add(model.getLiquidity());
            hashes.add(model.getHash());
            dailyUsd.add(model.getDailyVolumeUsd());
            dailyBase.add(model.getDailyVolumeBase());
            dailyQuote.add(model.getDailyVolumeQuote());
            tvlUsd.add(model.getTvlUsd());
            tvlBase.add(model.getTvlBase());
            tvlQuote.add(model.getTvlQuote());
            tradingTypes.add(model.getTradingType());
        }

        saveByFields(
                new Pair<>(tradingPairIds, "trading_pair_id"),
                new Pair<>(baseContractIds, "base_contract_id"),
                new Pair<>(quoteContractIds, "quote_contract_id"),
                new Pair<>(isActive, "is_active"),
                new Pair<>(basePrices, "base_price"),
                new Pair<>(quotePrices, "quote_price"),
                new Pair<>(fees, "fee_percentage"),
                new Pair<>(liquidity, "liquidity"),
                new Pair<>(hashes, "hash"),
                new Pair<>(dailyUsd, "daily_volume_usd"),
                new Pair<>(dailyBase, "daily_volume_base"),
                new Pair<>(dailyQuote, "daily_volume_quote"),
                new Pair<>(tvlUsd, "tvl_usd"),
                new Pair<>(tvlBase, "tvl_base"),
                new Pair<>(tvlQuote, "tvl_quote"),
                new Pair<>(tradingTypes, "trading_type")
        );
    }

    public void update(Iterable<SwapModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> tradingPairIds = new LinkedList<>();
        List<Integer> baseContractIds = new LinkedList<>();
        List<Integer> quoteContractIds = new LinkedList<>();
        List<Boolean> isActive = new LinkedList<>();
        List<Double> basePrices = new LinkedList<>();
        List<Double> quotePrices = new LinkedList<>();
        List<Double> fees = new LinkedList<>();
        List<Double> liquidity = new LinkedList<>();
        List<String> hashes = new LinkedList<>();
        List<Double> dailyUsd = new LinkedList<>();
        List<Double> dailyBase = new LinkedList<>();
        List<Double> dailyQuote = new LinkedList<>();
        List<Double> tvlUsd = new LinkedList<>();
        List<Double> tvlBase = new LinkedList<>();
        List<Double> tvlQuote = new LinkedList<>();
        List<String> tradingTypes = new LinkedList<>();
        for (SwapModel model : models) {
            ids.add(model.getId());
            tradingPairIds.add(model.getTradingPairId());
            baseContractIds.add(model.getBaseContractId());
            quoteContractIds.add(model.getQuoteContractId());
            isActive.add(model.getIsActive());
            basePrices.add(model.getBasePrice());
            quotePrices.add(model.getQuotePrice());
            fees.add(model.getFeePercentage());
            liquidity.add(model.getLiquidity());
            hashes.add(model.getHash());
            dailyUsd.add(model.getDailyVolumeUsd());
            dailyBase.add(model.getDailyVolumeBase());
            dailyQuote.add(model.getDailyVolumeQuote());
            tvlUsd.add(model.getTvlUsd());
            tvlBase.add(model.getTvlBase());
            tvlQuote.add(model.getTvlQuote());
            tradingTypes.add(model.getTradingType());
        }

        List<Integer> foundIds = getById(ids).stream().map(SwapModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(tradingPairIds, "trading_pair_id"),
                new Pair<>(baseContractIds, "base_contract_id"),
                new Pair<>(quoteContractIds, "quote_contract_id"),
                new Pair<>(isActive, "is_active"),
                new Pair<>(basePrices, "base_price"),
                new Pair<>(quotePrices, "quote_price"),
                new Pair<>(fees, "fee_percentage"),
                new Pair<>(liquidity, "liquidity"),
                new Pair<>(hashes, "hash"),
                new Pair<>(dailyUsd, "daily_volume_usd"),
                new Pair<>(dailyBase, "daily_volume_base"),
                new Pair<>(dailyQuote, "daily_volume_quote"),
                new Pair<>(tvlUsd, "tvl_usd"),
                new Pair<>(tvlBase, "tvl_base"),
                new Pair<>(tvlQuote, "tvl_quote"),
                new Pair<>(tradingTypes, "trading_type"),
                new Pair<>(ids, "id")
        );
    }

    public List<SwapModel> getByTradingPairsIds(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "trading_pair_id"));
    }
}
