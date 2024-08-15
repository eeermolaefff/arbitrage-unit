package com.stambul.initializers.jobs.parsers.entities.interfaces;

import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.List;

public interface BatchParser<T, D extends DataObject<D>> {
    void parse(List<T> batch, long delayMills);
}
