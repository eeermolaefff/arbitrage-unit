package com.stambul.library.database.interaction.dao.interfaces;

import java.util.List;

public interface DAO<T> {
    List<T> getAll();
    T getById(int id);
    List<T> getById(Iterable<Integer> idsList);
    List<T> getByFields(T object);
    List<T> getByFields(Iterable<T> objectsList);
    void save(T object);
    void save(Iterable<T> objectsList);
    void update(T object);
    void update(Iterable<T> objectsList);
    void delete(int id);
    void delete(Iterable<Integer> idsList);
    void deleteAll();
}
