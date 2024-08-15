package com.stambul.library.database.interaction.services.interfaces;

import java.util.Map;

public interface Service<DTO> {

    /**
     * Adds object to the database
     * @param object an object you want to add
     * @throws IllegalArgumentException if object with such fields already exists in database or input data is invalid
     */
    void addToDatabase(DTO object);

    /**
     * Adds objects to the database
     * @param objects a sequence of objects you want to add
     * @throws IllegalArgumentException if objects with such fields already exists in database or input data is invalid
     */
    void addToDatabase(Iterable<DTO> objects);

    /**
     * Adds non-existent object to the database
     * @param object the object you want to add
     * @throws IllegalArgumentException if input data is invalid
     */
    void addNonExistingToDatabase(DTO object);

    /**
     * Adds non-existent objects to the database
     * @param objects the sequence of objects you want to add
     * @throws IllegalArgumentException if input data is invalid
     */
    void addNonExistingToDatabase(Iterable<DTO> objects);

    /**
     * Updates object in the database with specified id by new fields
     * @param object an object you want to update
     * @throws IllegalArgumentException if object not found by id in database or input data is invalid
     */
    void updateInDatabase(DTO object);

    /**
     * Updates objects in the database with specified ids by new fields
     * @param objects a sequence of objects you want to update
     * @throws IllegalArgumentException if any object of sequence not found by id in database or input data is invalid
     */
    void updateInDatabase(Iterable<DTO> objects);

    /**
     * Deletes an object from the database by specified id
     * @param object the object you want to delete
     * @throws IllegalArgumentException if object id not found in database or input data is invalid
     */
    void removeFromDatabase(DTO object);

    /**
     * Deletes objects from the database by specified ids
     * @param objects the sequence of objects you want to delete
     * @throws IllegalArgumentException if any object id of sequence not found in database or input data is invalid
     */
    void removeFromDatabase(Iterable<DTO> objects);

    /**
     * Creates a map of all objects from database and their ids
     * @return a map of &lt;Integer id, T object&gt;
     */
    Map<Integer, DTO> getMap();

    /**
     * Creates a map of objects from database and their ids by specified ids sequence
     * @param ids the sequence of ids whose objects you want to get
     * @return a map of &lt;Integer id, T object&gt;
     * @throws IllegalArgumentException if any id of sequence not found in database or input data is invalid
     */
    Map<Integer, DTO> getMap(Iterable<Integer> ids);
}
