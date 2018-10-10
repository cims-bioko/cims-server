package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Task;

import java.util.Optional;

public interface SyncService {
    void scheduleTask(String schedule);
    boolean cancelTask();
    boolean isTaskScheduled();
    boolean isTaskRunning();
    boolean requestTaskRun();
    Task getTask();
    Optional<Long> getMinutesToNextRun();
}
