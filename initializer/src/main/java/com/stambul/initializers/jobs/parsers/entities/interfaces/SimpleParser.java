package com.stambul.initializers.jobs.parsers.entities.interfaces;

import com.stambul.library.database.objects.interfaces.DataObject;

public interface SimpleParser<D extends DataObject<D>> {
    void parse();
}
