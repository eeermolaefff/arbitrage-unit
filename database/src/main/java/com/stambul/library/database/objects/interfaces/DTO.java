package com.stambul.library.database.objects.interfaces;

public interface DTO<T> extends DataObject<T> {
    void updateFields(T newObject);
}
