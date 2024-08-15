package com.stambul.arbitrageur.jobs.events;

import com.stambul.arbitrageur.arbitrage.cycles.objects.FieldsComparableCycle;
import com.stambul.arbitrageur.arbitrage.cycles.objects.ProfitComparableCycle;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class ArbitrageurFinishEvent extends ApplicationEvent {
    private final Map<FieldsComparableCycle, ProfitComparableCycle> foundCycles;

    public ArbitrageurFinishEvent(
            Object source,
            Map<FieldsComparableCycle, ProfitComparableCycle> foundCycles
    ) {
        super(source);
        this.foundCycles = foundCycles;
    }

    public Map<FieldsComparableCycle, ProfitComparableCycle> getFoundCycles() {
        return foundCycles;
    }


    @Override
    public String toString() {
        return "JobFinishEvent{" +
                "source=" + source.getClass().getSimpleName() +
                "foundCycles=" + foundCycles +
                '}';
    }
}