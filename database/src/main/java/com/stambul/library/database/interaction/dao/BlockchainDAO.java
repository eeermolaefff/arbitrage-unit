package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.BlockchainModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class BlockchainDAO extends ParentDAO<BlockchainModel> {

    @Autowired
    public BlockchainDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "blockchains";
    };

    @Override
    protected BeanPropertyRowMapper<BlockchainModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(BlockchainModel.class);
    }

    @Override
    protected void getValidation(BlockchainModel model) {
        if (model == null)
            throw new IllegalArgumentException("Blockchain should not be null");
        if (model.getName() == null)
            throw new IllegalArgumentException("Blockchain name should not be null: " + model);
    }
    @Override
    protected void saveValidation(BlockchainModel model) {
        getValidation(model);
        if (model.getBaseCoinId() == null)
            throw new IllegalArgumentException("Blockchain base coin id should not be null: " + model);
    }

    @Override
    protected void updateValidation(BlockchainModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Blockchain id should not be null: " + model);
    }

    public List<BlockchainModel> getByFields(Iterable<BlockchainModel> models) {
        validate(models, this::getValidation);

        List<String> names = new LinkedList<>();
        for (BlockchainModel model : models)
            names.add(model.getName());

        return getByFields(new Pair<>(names, "name"));
    }

    public void save(Iterable<BlockchainModel> models) {
        validate(models, this::saveValidation);

        List<BlockchainModel> foundBlockchains = getByFields(models);
        if (!foundBlockchains.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundBlockchains;
            throw new IllegalArgumentException(message);
        }

        List<Integer> ids = new LinkedList<>();
        List<Integer> baseIds = new LinkedList<>();
        List<Double> gases = new LinkedList<>();
        List<String> names = new LinkedList<>();
        for (BlockchainModel model : models) {
            ids.add(model.getId());
            names.add(model.getName());
            baseIds.add(model.getBaseCoinId());
            gases.add(model.getGas());
        }
        saveByFields(
                new Pair<>(ids, "id"),
                new Pair<>(names, "name"),
                new Pair<>(baseIds, "base_coin_id"),
                new Pair<>(gases, "gas")
        );
    }

    public void update(Iterable<BlockchainModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<Integer> baseIds = new LinkedList<>();
        List<Double> gases = new LinkedList<>();
        for (BlockchainModel model : models) {
            ids.add(model.getId());
            names.add(model.getName());
            baseIds.add(model.getBaseCoinId());
            gases.add(model.getGas());
        }

        List<Integer> foundIds = getById(ids).stream().map(BlockchainModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(baseIds, "base_coin_id"),
                new Pair<>(gases, "gas"),
                new Pair<>(names, "name"),
                new Pair<>(ids, "id")
        );
    }
}
