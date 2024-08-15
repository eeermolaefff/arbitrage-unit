package com.stambul.initializers.jobs.entities.interfaces;

import com.stambul.initializers.jobs.events.JobLaunchEvent;
import com.stambul.initializers.jobs.events.JobRebootEvent;
import com.stambul.initializers.jobs.events.ParserHandlerFinishEvent;;

public interface Job<J> extends Runnable {
    LaunchRequirements getLaunchRequirements();
    void launchOnEvent(JobLaunchEvent<J> event);
    void rebootOnEvent(JobRebootEvent<J> event);
    void finishOnEvent(ParserHandlerFinishEvent<J> event);
}
