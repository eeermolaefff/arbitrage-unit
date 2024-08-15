package com.stambul.initializers.jobs.events;

import org.springframework.context.ApplicationEvent;

public class JobFinishEvent extends ApplicationEvent {
    public JobFinishEvent(Object sender) {
        super(sender);
    }
}