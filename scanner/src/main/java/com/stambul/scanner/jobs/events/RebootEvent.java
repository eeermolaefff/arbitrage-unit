package com.stambul.scanner.jobs.events;

import org.springframework.context.ApplicationEvent;

public class RebootEvent extends ApplicationEvent {
    private final Exception interruptingException;
    private final Class<?> job;

    public RebootEvent(
            Object source,
            Class<?> job,
            Exception interruptingException
    ) {
        super(source);
        this.job = job;
        this.interruptingException = interruptingException;
    }

    public Exception getInterruptingException() {
        return interruptingException;
    }

    public Class<?> getJob() {
        return job;
    }

    @Override
    public String toString() {
        String format = "RebootEvent[job=%s, interruptingException=%s]";
        return String.format(format, getJob().getName(), interruptingException);
    }
}