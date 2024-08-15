package com.stambul.library.database.interaction.dao.interfaces;

import com.stambul.library.tools.IterableTools;
import com.stambul.library.tools.Pair;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

public abstract class ParentDAO<T> implements DAO<T> {
    protected interface Handler<T> { void handle(T handledObject); }
    protected final JdbcTemplate jdbcTemplate;

    public ParentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    protected abstract void getValidation(T object);
    protected abstract void saveValidation(T object);
    protected abstract void updateValidation(T object);
    protected abstract String getTableName();
    protected abstract BeanPropertyRowMapper<T> getBeanPropertyRowMapper();


    public List<T> getAll() {
        return jdbcTemplate.query( "SELECT * FROM " + getTableName(), getBeanPropertyRowMapper());
    }

    public T getById(int id) {
        return getById(List.of(id)).stream().findAny().orElse(null);
    }

    public List<T> getById(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);
        return getByFields(new Pair<>(idList, "id"));
    }

    public List<T> getByFields(T object) {
        return getByFields(List.of(object));
    }

    public void save(T object) {
        save(List.of(object));
    }

    public void update(T object) {
        update(List.of(object));
    }

    public void delete(int object) {
        delete(List.of(object));
    }

    public void delete(Iterable<Integer> idList) {
        validate(idList, this::fieldValidation);

        List<T> foundObjects = getByFields(new Pair<>(idList, "id"));
        if (foundObjects.size() != IterableTools.size(idList)) {
            String message = "Search objects size and found objects size mismatch:";
            message += "foundObjects=" + foundObjects + " idList=" + idList;
            throw new IllegalArgumentException(message);
        }

        deleteByFields(new Pair<>(idList, "id"));
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM " + getTableName() + ";");
    }

    private Pair<List<String>, List<Iterator<?>>> prepareFields(Pair<Iterable<?>, String>... fields) {
        List<String> fieldNames = new ArrayList<>();
        List<Iterator<?>> fieldIterators = new ArrayList<>();

        int fieldValuesSize = -1;
        for (Pair<Iterable<?>, String> field : fields) {
            Iterable<?> values = field.getFirst();
            String name = field.getSecond();

            if (fieldValuesSize == -1) {
                fieldValuesSize = IterableTools.size(values);
            } else if (fieldValuesSize != IterableTools.size(values)) {
                String message = "Fields values batch sizes are not equal: " + Arrays.toString(fields);
                throw new IllegalArgumentException(message);
            }

            fieldNames.add(name);
            fieldIterators.add(values.iterator());
        }

        return new Pair<>(fieldNames, fieldIterators);
    }

    protected List<T> getByFields(Pair<Iterable<?>, String>... fields) {
        Pair<List<String>, List<Iterator<?>>> preparedFields = prepareFields(fields);
        List<String> fieldNames = preparedFields.getFirst();
        List<Iterator<?>> fieldIterators = preparedFields.getSecond();

        List<Object> batch = new LinkedList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + getTableName() + " WHERE ");
        String separator = " OR ";
        String searchPattern = buildSearchPattern(fieldNames) + separator;
        while (fieldIterators.get(0).hasNext()) {
            sqlBuilder.append(searchPattern);
            for (Iterator<?> iterator : fieldIterators)
                batch.add(iterator.next());
        }
        sqlBuilder.replace(sqlBuilder.length() - separator.length(), sqlBuilder.length(), "");

        return jdbcTemplate.query(sqlBuilder.toString(), getBeanPropertyRowMapper(), batch.toArray());
    }

    private String buildSearchPattern(List<String> fieldNames) {
        String separator = " AND ";

        StringBuilder searchBuilder = new StringBuilder("(");
        for (String name : fieldNames)
            searchBuilder.append(name).append("=?").append(separator);
        searchBuilder.replace(searchBuilder.length() - separator.length(), searchBuilder.length(), "");
        searchBuilder.append(")");

        return searchBuilder.toString();
    }

    protected void saveByFields(Pair<Iterable<?>, String>... fields) {
        Pair<List<String>, List<Iterator<?>>> preparedFields = prepareFields(fields);
        List<String> fieldNames = preparedFields.getFirst();
        List<Iterator<?>> fieldIterators = preparedFields.getSecond();

        List<Object[]> batch = new LinkedList<>();
        String saveRequest = buildSaveRequest(fieldNames);

        while (fieldIterators.get(0).hasNext()) {
            Object[] values = new Object[fieldNames.size()];
            for (int i = 0; i < fieldIterators.size(); i++)
                values[i] = fieldIterators.get(i).next();
            batch.add(values);
        }

        jdbcTemplate.batchUpdate(saveRequest, batch);
    }

    private String buildSaveRequest(List<String> fieldNames) {
        String separator = ", ";

        StringBuilder insertBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();
        insertBuilder.append("INSERT INTO ").append(getTableName()).append("(");
        valuesBuilder.append("VALUES(");

        for (String name : fieldNames) {
            insertBuilder.append(name).append(separator);
            valuesBuilder.append("?").append(separator);
        }
        insertBuilder.replace(insertBuilder.length() - separator.length(), insertBuilder.length(), "");
        insertBuilder.append(")");
        valuesBuilder.replace(valuesBuilder.length() - separator.length(), valuesBuilder.length(), "");
        valuesBuilder.append(")");

        return insertBuilder.append(" ").append(valuesBuilder).toString();
    }

    protected void updateByFields(Pair<Iterable<?>, String>... fields) {    // last field must be id
        Pair<List<String>, List<Iterator<?>>> preparedFields = prepareFields(fields);
        List<String> fieldNames = preparedFields.getFirst();
        List<Iterator<?>> fieldIterators = preparedFields.getSecond();

        List<Object[]> batch = new LinkedList<>();
        String updateRequest = buildUpdateRequest(fieldNames);

        while (fieldIterators.get(0).hasNext()) {
            Object[] values = new Object[fieldNames.size()];
            for (int i = 0; i < fieldIterators.size(); i++)
                values[i] = fieldIterators.get(i).next();
            batch.add(values);
        }

        jdbcTemplate.batchUpdate(updateRequest, batch);
    }

    private String buildUpdateRequest(List<String> fieldNames) {
        String separator = ", ";

        StringBuilder updateBuilder = new StringBuilder();
        updateBuilder.append("UPDATE ").append(getTableName()).append(" SET ");

        for (String name : fieldNames.subList(0, fieldNames.size() - 1))
            updateBuilder.append(name).append("=?").append(separator);
        updateBuilder.replace(updateBuilder.length() - separator.length(), updateBuilder.length(), "");
        updateBuilder.append(" WHERE id=?");

        return updateBuilder.toString();
    }



    protected void deleteByFields(Pair<Iterable<?>, String>... fields) {    // last field must be id
        Pair<List<String>, List<Iterator<?>>> preparedFields = prepareFields(fields);
        List<String> fieldNames = preparedFields.getFirst();
        List<Iterator<?>> fieldIterators = preparedFields.getSecond();

        List<Object[]> batch = new LinkedList<>();
        String deleteRequest = buildDeleteRequest(fieldNames);

        while (fieldIterators.get(0).hasNext()) {
            Object[] values = new Object[fieldNames.size()];
            for (int i = 0; i < fieldIterators.size(); i++)
                values[i] = fieldIterators.get(i).next();
            batch.add(values);
        }

        jdbcTemplate.batchUpdate(deleteRequest, batch);
    }

    private String buildDeleteRequest(List<String> fieldNames) {
        String separator = ", ";

        StringBuilder deleteBuilder = new StringBuilder();
        deleteBuilder.append("DELETE FROM ").append(getTableName()).append(" WHERE ");

        for (String name : fieldNames)
            deleteBuilder.append(name).append("=?").append(separator);
        deleteBuilder.replace(deleteBuilder.length() - separator.length(), deleteBuilder.length(), "");

        return deleteBuilder.toString();
    }

    protected <M> void validate(Iterable<M> objects, Handler<M> validator) {
        if (objects == null)
            throw new IllegalArgumentException("Objects list should not be null");
        if (IterableTools.size(objects) == 0)
            throw new IllegalArgumentException("Objects list should not be empty: " + objects);
        for (M object : objects)
            validator.handle(object);
    }

    protected <M> void fieldValidation(M field) {
        if (field == null)
            throw new IllegalArgumentException("Field should not be null");
    };
}
