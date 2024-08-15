package com.stambul.initializers.jobs.events;

public class JobLaunchEvent<T> extends DirectMessage<T> {
    public JobLaunchEvent(Object sender, Class<T> recipient) {
        super(sender, recipient);
    }
}