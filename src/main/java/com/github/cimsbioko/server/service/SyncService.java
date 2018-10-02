package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Task;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public interface SyncService {
    void scheduleTask();
    boolean cancelTask();
    Task getTask();
    File getSyncFile();
    void generateMobileDb(File dest) throws IOException, SQLException, NoSuchAlgorithmException;
    void generateMobileDb();
    Optional<Long> getMinutesToNextRun();
}
