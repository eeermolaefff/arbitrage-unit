package com.stambul.library.tools;


import com.stambul.library.database.objects.interfaces.Identifiable;

public class IdentifiableObject implements Identifiable {
    private final Integer id;
    private final Object values;

    public IdentifiableObject(Integer id, Object values) {
        this.id = id;
        this.values = values;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("{\"id\": %d, \"values\": %s}", id, values.toString());
    }
}
