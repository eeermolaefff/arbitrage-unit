package com.stambul.initializers.jobs.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public class DirectMessage<T> extends ApplicationEvent implements ResolvableTypeProvider {
    protected final Class<T> recipient;

    public DirectMessage(Object sender, Class<T> recipient) {
        super(sender);
        this.recipient = recipient;
    }

    public boolean recipientMatch(Class<?> recipient) {
        return this.recipient.equals(recipient);
    }

    public Class<?> getRecipient() {
        return recipient;
    }

    protected String toStringAdditional() {
        return "";
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                this.getClass(), ResolvableType.forClass(recipient)
        );
    }

    @Override
    public String toString() {
        int maxLen = 1000;
        String additional = toStringAdditional();
        if (additional.length() > maxLen)
            additional = ", " + additional.substring(0, maxLen) + "[...]";
        String format = this.getClass().getSimpleName() + "[sender=%s, recipient=%s" + additional + "]";
        return String.format(format, getSource().getClass().getName(), recipient.getName());
    }
}
