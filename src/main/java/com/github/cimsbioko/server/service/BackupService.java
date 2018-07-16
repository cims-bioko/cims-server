package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Backup;

import java.util.List;

public interface BackupService {
    void createBackup(String name, String description);
    List<Backup> getBackups();
}
