package com.stambul.initializers.jobs.events;

public class ParserHandlerFinishEvent<J> extends DirectMessage<J> {
    public ParserHandlerFinishEvent(Object sender, Class<J> recipient) {
        super(sender, recipient);
    }
}