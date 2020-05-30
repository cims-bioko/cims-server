package com.github.cimsbioko.server.service;

import java.io.File;
import java.util.Optional;

public interface SyncService {
    enum Status {
        SCHEDULED, RUNNING, PAUSED, NO_SYNC
    }
    interface Task {
        int getPercentComplete();
        Status getStatus();
        String getContentHash();
        long getNextRunMinutes();
    }
    Status getStatus(String campaign);
    File getOutput(String campaign);
    void requestExport(String campaign);
    Optional<Task> getTask(String campaign);
    void pauseSync(String campaign);
    void resumeSync(String campaign);
}
