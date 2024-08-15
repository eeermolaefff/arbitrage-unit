package com.stambul.initializers.jobs.exceptions;

import org.apache.log4j.Logger;
import org.springframework.util.ErrorHandler;

public class UncaughtExceptionHandler implements ErrorHandler {
    private final Logger logger = Logger.getLogger(this.getClass());
    @Override
    public void handleError(Throwable t) {
        logger.error("An unexpected exception has caught", t);
    }
}
