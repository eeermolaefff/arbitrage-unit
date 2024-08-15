package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.ContractModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ContractDAO extends ParentDAO<ContractModel> {
    @Autowired
    public ContractDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "contracts";
    };

    @Override
    protected BeanPropertyRowMapper<ContractModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(ContractModel.class);
    }

    @Override
    protected void getValidation(ContractModel model) {
        if (model == null)
            throw new IllegalArgumentException("Contract should not be null");
        if (model.getAddress() == null || model.getBlockchainId() == null || model.getCurrencyId() == null) {
            String message = "Contract address, blockchain id and currency id should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(ContractModel model) {
        getValidation(model);
    }

    @Override
    protected void updateValidation(ContractModel model) {
        getValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Contract id should not be null: " + model);
    }

    public List<ContractModel> getByCurrencyId(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "currency_id"));
    }

    public List<ContractModel> getByBlockchainId(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "blockchain_id"));
    }

    public List<ContractModel> getByFields(Iterable<ContractModel> models) {
        validate(models, this::getValidation);

        List<String> addresses = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        for (ContractModel model : models) {
            addresses.add(model.getAddress());
            blockchainIds.add(model.getBlockchainId());
            currencyIds.add(model.getCurrencyId());
        }

        return getByFields(
                new Pair<>(addresses, "address"),
                new Pair<>(blockchainIds, "blockchain_id"),
                new Pair<>(currencyIds, "currency_id")
        );
    }

    public void save(Iterable<ContractModel> models) {
        validate(models, this::saveValidation);

        List<ContractModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<String> addresses = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        for (ContractModel model : models) {
            addresses.add(model.getAddress());
            blockchainIds.add(model.getBlockchainId());
            currencyIds.add(model.getCurrencyId());
        }

        saveByFields(
                new Pair<>(addresses, "address"),
                new Pair<>(blockchainIds, "blockchain_id"),
                new Pair<>(currencyIds, "currency_id")
        );
    }

    public void update(Iterable<ContractModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<String> addresses = new LinkedList<>();
        List<Integer> blockchainIds = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        for (ContractModel model : models) {
            ids.add(model.getId());
            addresses.add(model.getAddress());
            blockchainIds.add(model.getBlockchainId());
            currencyIds.add(model.getCurrencyId());
        }

        List<Integer> foundIds = getById(ids).stream().map(ContractModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(addresses, "address"),
                new Pair<>(blockchainIds, "blockchain_id"),
                new Pair<>(currencyIds, "currency_id"),
                new Pair<>(ids, "id")
        );
    }
}
