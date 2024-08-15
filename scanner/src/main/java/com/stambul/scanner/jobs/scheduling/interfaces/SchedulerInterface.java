package com.stambul.scanner.jobs.scheduling.interfaces;

import com.stambul.scanner.jobs.events.JobFinishEvent;
import com.stambul.scanner.jobs.events.RebootEvent;

public interface SchedulerInterface extends Runnable {
    void handleRebootEvent(RebootEvent event);
    void handleFinishEvent(JobFinishEvent event);
}
