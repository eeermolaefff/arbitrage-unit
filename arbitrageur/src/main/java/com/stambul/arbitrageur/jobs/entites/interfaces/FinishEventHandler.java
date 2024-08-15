package com.stambul.arbitrageur.jobs.entites.interfaces;

import com.stambul.arbitrageur.jobs.events.ArbitrageurFinishEvent;

public interface FinishEventHandler {
    void handleFinishEvent(ArbitrageurFinishEvent event);
}
