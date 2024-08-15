package com.stambul.initializers.jobs.entities.interfaces;

import com.stambul.initializers.jobs.events.JobLaunchEvent;

import java.util.HashSet;
import java.util.Set;

public class LaunchRequirements {
    private final Set<String> requiredJobs = new HashSet<>();
    private final JobLaunchEvent<?> jobLaunchEvent;

    public LaunchRequirements(Iterable<Class> requirements, JobLaunchEvent<?> jobLaunchEvent) {
        for (Class requirement : requirements)
            requiredJobs.add(requirement.getName());
        this.jobLaunchEvent = jobLaunchEvent;
    }

    public void satisfy(Class<?> readyJob) {
        if (requiredJobs.isEmpty())
            return;
        requiredJobs.remove(readyJob.getName());
    }

    public boolean isSatisfied() {
        return requiredJobs.isEmpty();
    }

    public JobLaunchEvent<?> getLaunchEvent() {
        return jobLaunchEvent;
    }

    @Override
    public String toString() {
        String format = "LaunchRequirements[jobLaunchEvent=%s, requiredJobs=%s]";
        return String.format(format, jobLaunchEvent, requiredJobs);
    }
}
