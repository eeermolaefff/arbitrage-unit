package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.TransferModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class TransferDAO extends ParentDAO<TransferModel> {
    @Autowired
    public TransferDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "transfers";
    };

    @Override
    protected BeanPropertyRowMapper<TransferModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(TransferModel.class);
    }

    @Override
    protected void getValidation(TransferModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model should not be null");
        if (model.getBlockchainId() == null) {
            String message = "Model blockchain id should not be null: model=" + model;
            throw new IllegalArgumentException(message);
        }
        if (model.getMarketCurrencyRelationId() == null) {
            String message = "Model relation id should not be null: model=" + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void saveValidation(TransferModel model) {
        getValidation(model);
    }

    @Override
    protected void updateValidation(TransferModel model) {
        getValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Model id should not be null: " + model);
    }

    public List<TransferModel> getByFields(Iterable<TransferModel> models) {
        validate(models, this::getValidation);

        List<Integer> relationIds = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        for (TransferModel model : models) {
            relationIds.add(model.getMarketCurrencyRelationId());
            blockchainIds.add(model.getBlockchainId());
        }

        return getByFields(
                new Pair<>(relationIds, "market_currency_relation_id"),
                new Pair<>(blockchainIds, "blockchain_id")
        );
    }

    public void save(Iterable<TransferModel> models) {
        validate(models, this::saveValidation);

        List<TransferModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> relationIds = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        List<Boolean> withdrawFlags = new LinkedList<>();
        List<Boolean> depositFlags = new LinkedList<>();
        List<Double> withdrawalStaticCommissions = new LinkedList<>();
        List<Double> depositStaticCommissions = new LinkedList<>();
        List<Double> withdrawalPercentCommissions = new LinkedList<>();
        List<Double> depositPercentCommissions = new LinkedList<>();
        for (TransferModel model : models) {
            relationIds.add(model.getMarketCurrencyRelationId());
            blockchainIds.add(model.getBlockchainId());
            withdrawFlags.add(model.getWithdrawEnable());
            depositFlags.add(model.getDepositEnable());
            withdrawalStaticCommissions.add(model.getWithdrawalStaticCommission());
            depositStaticCommissions.add(model.getDepositStaticCommission());
            withdrawalPercentCommissions.add(model.getWithdrawalPercentCommission());
            depositPercentCommissions.add(model.getDepositPercentCommission());
        }

        saveByFields(
                new Pair<>(relationIds, "market_currency_relation_id"),
                new Pair<>(blockchainIds, "blockchain_id"),
                new Pair<>(withdrawFlags, "withdraw_enable"),
                new Pair<>(depositFlags, "deposit_enable"),
                new Pair<>(withdrawalStaticCommissions, "withdrawal_static_commission"),
                new Pair<>(depositStaticCommissions, "deposit_static_commission"),
                new Pair<>(withdrawalPercentCommissions, "withdrawal_percent_commission"),
                new Pair<>(depositPercentCommissions, "deposit_percent_commission")
        );
    }

    public void update(Iterable<TransferModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> relationIds = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        List<Boolean> withdrawFlags = new LinkedList<>();
        List<Boolean> depositFlags = new LinkedList<>();
        List<Double> withdrawalStaticCommissions = new LinkedList<>();
        List<Double> depositStaticCommissions = new LinkedList<>();
        List<Double> withdrawalPercentCommissions = new LinkedList<>();
        List<Double> depositPercentCommissions = new LinkedList<>();
        for (TransferModel model : models) {
            ids.add(model.getId());
            relationIds.add(model.getMarketCurrencyRelationId());
            blockchainIds.add(model.getBlockchainId());
            withdrawFlags.add(model.getWithdrawEnable());
            depositFlags.add(model.getDepositEnable());
            withdrawalStaticCommissions.add(model.getWithdrawalStaticCommission());
            depositStaticCommissions.add(model.getDepositStaticCommission());
            withdrawalPercentCommissions.add(model.getWithdrawalPercentCommission());
            depositPercentCommissions.add(model.getDepositPercentCommission());
        }

        List<Integer> foundIds = getById(ids).stream().map(TransferModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(relationIds, "market_currency_relation_id"),
                new Pair<>(blockchainIds, "blockchain_id"),
                new Pair<>(withdrawFlags, "withdraw_enable"),
                new Pair<>(depositFlags, "deposit_enable"),
                new Pair<>(withdrawalStaticCommissions, "withdrawal_static_commission"),
                new Pair<>(depositStaticCommissions, "deposit_static_commission"),
                new Pair<>(withdrawalPercentCommissions, "withdrawal_percent_commission"),
                new Pair<>(depositPercentCommissions, "deposit_percent_commission"),
                new Pair<>(ids, "id")
        );
    }
}
