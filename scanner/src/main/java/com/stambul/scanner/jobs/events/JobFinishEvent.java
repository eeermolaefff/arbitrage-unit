package com.stambul.scanner.jobs.events;

import com.stambul.scanner.jobs.entities.interfaces.Job;
import org.springframework.context.ApplicationEvent;

public class JobFinishEvent extends ApplicationEvent {
    private final Class<?> job;

    public JobFinishEvent(
            Object source,
            Class<?> job
    ) {
        super(source);
        this.job = job;
    }

    public Class<?> getJob() {
        return job;
    }

    @Override
    public String toString() {
        String format = this.getClass().getSimpleName() + "[job=%s]";
        return String.format(format, getJob().getName());
    }
}