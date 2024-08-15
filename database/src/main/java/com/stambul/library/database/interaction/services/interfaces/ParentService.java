package com.stambul.library.database.interaction.services.interfaces;

import com.stambul.library.database.interaction.dao.interfaces.DAO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.tools.IterableTools;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class ParentService <Model extends DataObject<Model>, DTO extends DataObject<DTO>> implements Service<DTO> {
    protected interface Handler<T> { void handle(T handledObject); }
    protected final DAO<Model> dao;
    protected final int batchSize;
    protected final Logger logger = Logger.getLogger(this.getClass());

    public ParentService(DAO<Model> dao, int batchSize) {
        this.dao = dao;
        this.batchSize = batchSize;
    }

    protected abstract void addValidation(DTO object);
    protected abstract void updateValidation(DTO object);
    protected abstract void removeValidation(DTO object);
    protected abstract List<Model> toModelsList(Iterable<DTO> dtos);
    protected abstract Map<Integer, Model> toModelsMap(Iterable<DTO> dtos);
    protected abstract List<DTO> toDTOsList(Iterable<Model> models);
    protected abstract Map<Integer, DTO> toDTOsMap(Iterable<Model> models);

    protected void beforeAddingActions(Iterable<DTO> dtos) {};
    protected void beforeUpdatingActions(Iterable<DTO> dtos) {};
    protected void beforeRemovingActions(Iterable<DTO> dtos) {};
    protected void afterAddingActions(Iterable<DTO> dtos) {};
    protected void afterUpdatingActions(Iterable<DTO> dtos) {};
    protected void afterRemovingActions(Iterable<DTO> dtos) {};
    protected void beforeAddingOrUpdatingActions(Iterable<DTO> dtos) {};
    protected void afterAddingOrUpdatingActions(Iterable<DTO> dtos) {};


    public void addToDatabase(DTO object) {
        addToDatabase(List.of(object));
    }

    public void addToDatabase(Iterable<DTO> objects) {
        int size = IterableTools.size(objects);
        if (size == 0)
            return;
        if (size > batchSize) {
            addToDatabase(objects, batchSize);
            return;
        }

        validate(objects, this::addValidation);
        beforeAddingActions(objects);

        List<Model> modelsToAdd = toModelsList(objects);
        List<Model> foundModels = dao.getByFields(modelsToAdd);
        if (!foundModels.isEmpty())
            throw new IllegalArgumentException("Models with such fields already exist: " + foundModels);

        dao.save(modelsToAdd);

        afterAddingActions(objects);
    }

    protected void addToDatabase(Iterable<DTO> objects, int batchSize) {
        String formatMessage = "%d/%d objects has added to database";
        batchAction(objects, batchSize, this::addToDatabase, formatMessage);
    }

    public void updateInDatabase(DTO object) { updateInDatabase(List.of(object)); }

    public void updateInDatabase(Iterable<DTO> objects) {
        int size = IterableTools.size(objects);
        if (size == 0)
            return;
        if (size > batchSize) {
            updateInDatabase(objects, batchSize);
            return;
        }

        validate(objects, this::updateValidation);
        beforeUpdatingActions(objects);

        Set<Integer> allIds = StreamSupport.stream(objects.spliterator(), false).map(DTO::getId).collect(Collectors.toSet());
        List<Model> foundModels = dao.getById(allIds);

        if (foundModels.size() != allIds.size()) {
            List<Integer> foundIds = foundModels.stream().map(Model::getId).toList();
            Set<Integer> notFound = IterableTools.minus(allIds, foundIds);
            throw new IllegalArgumentException("Models with such ids not found in database: " + notFound);
        }

        dao.update(toModelsMap(objects).values());

        afterUpdatingActions(objects);
    }

    protected void updateInDatabase(Iterable<DTO> objects, int batchSize) {
        String formatMessage = "%d/%d objects has updated in database";
        batchAction(objects, batchSize, this::updateInDatabase, formatMessage);
    }

    public void addNonExistingToDatabase(DTO object) {
        addNonExistingToDatabase(List.of(object));
    }

    public void addNonExistingToDatabase(Iterable<DTO> objects) {
        int size = IterableTools.size(objects);
        if (size == 0)
            return;
        if (size > batchSize) {
            addNonExistingToDatabase(objects, batchSize);
            return;
        }

        validate(objects, this::addValidation);
        beforeAddingActions(objects);

        List<Model> modelsToAdd = toModelsList(objects);
        List<Model> foundModels = dao.getByFields(modelsToAdd);

        Collection<Model> nonExisting = IterableTools.minus(modelsToAdd, foundModels);
        if (!nonExisting.isEmpty())
            dao.save(nonExisting);
        if (!foundModels.isEmpty())
            dao.update(foundModels);

        afterAddingActions(objects);
    }

    protected void addNonExistingToDatabase(Iterable<DTO> objects, int batchSize) {
        String formatMessage = "%d/%d objects has added to database";
        batchAction(objects, batchSize, this::addNonExistingToDatabase, formatMessage);
    }

    public void removeFromDatabase(DTO object) {
        removeFromDatabase(List.of(object));
    }

    public void removeFromDatabase(Iterable<DTO> objects) {
        int size = IterableTools.size(objects);
        if (size == 0)
            return;
        if (size > batchSize) {
            removeFromDatabase(objects, batchSize);
            return;
        }

        validate(objects, this::removeValidation);
        beforeRemovingActions(objects);

        Map<Integer, Model> modelsToRemove = toModelsMap(objects);
        List<Model> foundModels = dao.getById(modelsToRemove.keySet());
        List<Integer> foundIds = foundModels.stream().map(Model::getId).toList();
        if (foundModels.size() != modelsToRemove.size()) {
            Set<Integer> notFound = IterableTools.minus(modelsToRemove.keySet(), foundIds);
            throw new IllegalArgumentException("Models with such ids not found in database: " + notFound);
        }

        dao.delete(foundIds);

        afterRemovingActions(objects);
    }

    protected void removeFromDatabase(Iterable<DTO> objects, int batchSize) {
        String formatMessage = "%d/%d objects has removed from database";
        batchAction(objects, batchSize, this::removeFromDatabase, formatMessage);
    }

    public List<DTO> getAll() {
        return toDTOsList(dao.getAll());
    }

    public Map<Integer, DTO> getMap() {
        return toDTOsMap(dao.getAll());
    }

    public Map<Integer, DTO> getMap(Iterable<Integer> ids) {
        int size = IterableTools.size(ids);
        if (size == 0)
            return new TreeMap<>();
        if (size > batchSize)
            return getMap(ids, batchSize);

        validate(ids, this::fieldValidation);
        List<Model> foundModels = dao.getById(ids);
        return toDTOsMap(foundModels);
    }

    protected Map<Integer, DTO> getMap(Iterable<Integer> ids, int batchSize) {
        String formatMessage = "%d/%d objects has extracted from database";
        MapHandler handler = new MapHandler();
        batchAction(ids, batchSize, handler, formatMessage);
        return handler.getMap();
    }

    protected class MapHandler implements Handler<Iterable<Integer>> {
        private final Map<Integer, DTO> map = new TreeMap<>();
        @Override
        public void handle(Iterable<Integer> ids) {
            this.map.putAll(ParentService.this.getMap(ids));
        }
        public Map<Integer, DTO> getMap() {
            return map;
        }
    }

    protected <T> void batchAction(Iterable<T> handledObjects, int batchSize, Handler<Iterable<T>> handler, String formatMessage) {
        int size = IterableTools.size(handledObjects);
        if (size == 0)
            return;

        int i = 1;
        LinkedList<T> batch = new LinkedList<>();
        for (T object : handledObjects) {
            batch.add(object);
            if (i % batchSize == 0) {
//                logger.info(String.format(formatMessage, i, size));
                handler.handle(batch);
                batch.clear();
            }
            i++;
        }
//        logger.info(String.format(formatMessage, i, size));
        handler.handle(batch);
    }

    protected <T> void fieldValidation(T field) {
        if (field == null)
            throw new IllegalArgumentException("Field should not be null");
    };

    protected <T> void validate(Iterable<T> objects, Handler<T> validator) {
        if (objects == null)
            throw new IllegalArgumentException("Objects list should not be null");
        if (IterableTools.size(objects) == 0)
            throw new IllegalArgumentException("Objects list should not be empty: " + objects);
        for (T object : objects)
            validator.handle(object);
    }
}
