package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Task;

import java.io.File;
import java.util.Optional;

public interface SyncService {
    File getOutput();
    Optional<String> getSchedule();
    void scheduleTask(String schedule);
    void resumeSchedule();
    void cancelTask();
    boolean isTaskScheduled();
    boolean isTaskRunning();
    void requestTaskRun();
    Task getTask();
    Optional<Long> getMinutesToNextRun();
}
