package com.stambul.initializers.jobs.parsers.initializers.interfaces;

import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import org.apache.log4j.Logger;

public abstract class PrimaryInitializer<D extends DataObject<D>> implements ParsersInitializer {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final Class<?> job;
    private final SimpleParser<D> parser;
    private final int taskSize = 1;

    public PrimaryInitializer(Class<?> job, SimpleParser<D> parser) {
        this.job = job;
        this.parser = parser;
    }

    @Override
    public int initialize() {
        logger.debug(String.format("INITIALIZE(%s)", job.getSimpleName()));
        parser.parse();
        return taskSize;
    }
}
