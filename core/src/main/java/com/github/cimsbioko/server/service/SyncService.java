package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Task;

import java.io.File;

public interface SyncService {
    File getOutput(String campaign);
    void requestExport(String campaign);
    Task getTask(String campaign);
}
