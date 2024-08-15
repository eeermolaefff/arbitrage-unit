package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.TimestampModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class RelationTimestampDAO extends ParentDAO<TimestampModel> {
    @Autowired
    public RelationTimestampDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "relation_timestamps";
    };

    @Override
    protected BeanPropertyRowMapper<TimestampModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(TimestampModel.class);
    }

    @Override
    protected void getValidation(TimestampModel model) {
        if (model == null)
            throw new IllegalArgumentException("Relation timestamp should not be null");
        if (model.getParentId() == null) {
            String message = "Relation currency id should not be null: ";
            message += "model=" + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(TimestampModel model) {
        getValidation(model);
    }

    @Override
    protected void updateValidation(TimestampModel model) {
        getValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Relation timestamp id should not be null: " + model);
    }

    public List<TimestampModel> getByCurrencyId(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "parent_id"));
    }

    public List<TimestampModel> getByFields(Iterable<TimestampModel> models) {
        validate(models, this::getValidation);

        List<Integer> currencyIds = new LinkedList<>();
        for (TimestampModel model : models)
            currencyIds.add(model.getParentId());

        return getByFields(new Pair<>(currencyIds, "parent_id"));
    }

    public void save(Iterable<TimestampModel> models) {
        validate(models, this::saveValidation);

        List<TimestampModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> currencyIds = new LinkedList<>();
        for (TimestampModel model : models)
            currencyIds.add(model.getParentId());

        saveByFields(new Pair<>(currencyIds, "parent_id"));
    }
    public void update(Iterable<TimestampModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<Integer> currencyIds = new LinkedList<>();
        for (TimestampModel model : models) {
            ids.add(model.getId());
            currencyIds.add(model.getParentId());
        }

        List<Integer> foundIds = getById(ids).stream().map(TimestampModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(new Pair<>(currencyIds, "parent_id"), new Pair<>(ids, "id"));
    }
}
