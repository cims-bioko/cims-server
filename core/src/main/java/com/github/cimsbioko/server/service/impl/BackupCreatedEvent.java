package com.github.cimsbioko.server.service.impl;

public class BackupCreatedEvent implements BackupServiceEvent {

    private final String backupName;

    public BackupCreatedEvent(String backupName) {
        this.backupName = backupName;
    }

    public String getBackupName() {
        return backupName;
    }
}
