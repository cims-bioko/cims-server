package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Backup;

public interface BackupService {
    void createBackup(String name, String description);
    void deleteBackup(String name);
    Backup updateBackup(String name, Backup updated);
}
