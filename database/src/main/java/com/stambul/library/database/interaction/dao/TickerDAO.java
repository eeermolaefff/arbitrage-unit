package com.stambul.library.database.interaction.dao;

import com.stambul.library.database.interaction.dao.interfaces.ParentDAO;
import com.stambul.library.database.objects.models.TickerModel;
import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class TickerDAO extends ParentDAO<TickerModel> {
    @Autowired
    public TickerDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getTableName() {
        return "tickers";
    };

    @Override
    protected BeanPropertyRowMapper<TickerModel> getBeanPropertyRowMapper() {
        return new BeanPropertyRowMapper<>(TickerModel.class);
    }

    @Override
    protected void getValidation(TickerModel model) {
        if (model == null)
            throw new IllegalArgumentException("Ticker should not be null");
        if (model.getTicker() == null)
            throw new IllegalArgumentException("Ticker should not be null: " + model);
    }
    @Override
    protected void saveValidation(TickerModel model) {
        getValidation(model);
    }

    @Override
    protected void updateValidation(TickerModel model) {
        getValidation(model);
        if (model.getId() == null)
            throw new IllegalArgumentException("Ticker id should not be null: " + model);
    }

    public List<TickerModel> getByFields(Iterable<TickerModel> models) {
        validate(models, this::getValidation);

        List<String> names = new LinkedList<>();
        for (TickerModel model : models)
            names.add(model.getTicker());

        return getByFields(new Pair<>(names, "ticker"));
    }

    public void save(Iterable<TickerModel> models) {
        validate(models, this::saveValidation);

        List<TickerModel> foundModels = getByFields(models);
        if (!foundModels.isEmpty()) {
            String message = "Objects with such fields already exist in database: " + foundModels;
            throw new IllegalArgumentException(message);
        }

        List<String> tickerNames = new LinkedList<>();
        for (TickerModel model : models) {
            tickerNames.add(model.getTicker());
        }

        saveByFields(new Pair<>(tickerNames, "ticker"));
    }

    public void update(Iterable<TickerModel> models) {
        validate(models, this::updateValidation);

        List<Integer> ids = new LinkedList<>();
        List<String> tickerNames = new LinkedList<>();
        for (TickerModel model : models) {
            ids.add(model.getId());
            tickerNames.add(model.getTicker());
        }

        List<Integer> foundIds = getById(ids).stream().map(TickerModel::getId).toList();
        if (foundIds.size() != ids.size()) {
            var notFound = IterableTools.minus(ids, foundIds);
            String message = "Objects with such ids not found in database:";
            message += "notFound=" + notFound + " models=" + models;
            throw new IllegalArgumentException(message);
        }

        updateByFields(new Pair<>(tickerNames, "ticker"), new Pair<>(ids, "id"));
    }
}
