package com.stambul.initializers.jobs.scheduling.interfaces;

import com.stambul.initializers.jobs.events.JobFinishEvent;

public interface SchedulerInterface extends Runnable {
    void handleFinishEvent(JobFinishEvent event);
}
