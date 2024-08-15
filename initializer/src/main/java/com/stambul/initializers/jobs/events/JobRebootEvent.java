package com.stambul.initializers.jobs.events;

public class JobRebootEvent<T> extends DirectMessage<T> {
    private final String message;
    private final Exception cause;

    public JobRebootEvent(Object sender, Class<T> recipient, String message, Exception cause) {
        super(sender, recipient);
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public Exception getCause() {
        return cause;
    }

    @Override
    protected String toStringAdditional() {
        return String.format("message=%s, cause=%s", message, cause);
    }
}