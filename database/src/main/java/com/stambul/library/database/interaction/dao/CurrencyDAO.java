package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.CurrencyModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class CurrencyDAO extends ParentDAO<CurrencyModel> {
    @Autowired
    public CurrencyDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "currencies";
    };

    @Override
    protected BeanPropertyRowMapper<CurrencyModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(CurrencyModel.class);
    }

    @Override
    protected void getValidation(CurrencyModel model) {
        if (model == null)
            throw new IllegalArgumentException("Currency model should not be null");
        if (model.getSlug() == null) {
            String message = "Currency slug should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }
    @Override
    protected void saveValidation(CurrencyModel model) {
        getValidation(model);
        if (model.getFullName() == null) {
            String message = "Currency full name should not be null: " + model;
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    protected void updateValidation(CurrencyModel model) {
        saveValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Currency id should not be null: " + model);
    }


    public List<CurrencyModel> getByFields(Iterable<CurrencyModel> models) {
        validate(models, this::getValidation);

        List<String> slugs = new LinkedList<>();
        for (CurrencyModel model : models)
            slugs.add(model.getSlug());

        return getByFields(new Pair<>(slugs, "slug"));
    }

    public void save(Iterable<CurrencyModel> models) {
        validate(models, this::saveValidation);

        List<CurrencyModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<Integer> ids = new LinkedList<>();
        List<String > slugs = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<String> categories = new LinkedList<>();
        List<Double> cexVolumes = new LinkedList<>();
        List<Double> dexVolumes = new LinkedList<>();
        List<Double> capitalizations = new LinkedList<>();
        List<Boolean> active = new LinkedList<>();
        for (CurrencyModel model : models) {
            ids.add(model.getId());
            slugs.add(model.getSlug());
            names.add(model.getFullName());
            categories.add(model.getCategory());
            cexVolumes.add(model.getCexVolumeUsd());
            dexVolumes.add(model.getDexVolumeUsd());
            capitalizations.add(model.getMarketCapUsd());
            active.add(model.getIsActive());
        }

        saveByFields(
                new Pair<>(ids, "id"),
                new Pair<>(slugs, "slug"),
                new Pair<>(names, "full_name"),
                new Pair<>(categories, "category"),
                new Pair<>(cexVolumes, "cex_volume_usd"),
                new Pair<>(dexVolumes, "dex_volume_usd"),
                new Pair<>(capitalizations, "market_cap_usd"),
                new Pair<>(active, "is_active")
        );
    }

    public void update(Iterable<CurrencyModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<String> slugs = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<String> categories = new LinkedList<>();
        List<Double> cexVolumes = new LinkedList<>();
        List<Double> dexVolumes = new LinkedList<>();
        List<Double> capitalizations = new LinkedList<>();
        List<Boolean> active = new LinkedList<>();
        for (CurrencyModel model : models) {
            ids.add(model.getId());
            slugs.add(model.getSlug());
            names.add(model.getFullName());
            categories.add(model.getCategory());
            cexVolumes.add(model.getCexVolumeUsd());
            dexVolumes.add(model.getDexVolumeUsd());
            capitalizations.add(model.getMarketCapUsd());
            active.add(model.getIsActive());
        }


        List<Integer> foundIds = getById(ids).stream().map(CurrencyModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(
                new Pair<>(slugs, "slug"),
                new Pair<>(names, "full_name"),
                new Pair<>(categories, "category"),
                new Pair<>(cexVolumes, "cex_volume_usd"),
                new Pair<>(dexVolumes, "dex_volume_usd"),
                new Pair<>(capitalizations, "market_cap_usd"),
                new Pair<>(active, "is_active"),
                new Pair<>(ids, "id")
        );
    }
}
