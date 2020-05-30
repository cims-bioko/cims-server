package com.github.cimsbioko.server.service.impl.backup;

public class BackupCreatedEvent implements BackupServiceEvent {

    private final String backupName;

    public BackupCreatedEvent(String backupName) {
        this.backupName = backupName;
    }

    public String getBackupName() {
        return backupName;
    }
}
