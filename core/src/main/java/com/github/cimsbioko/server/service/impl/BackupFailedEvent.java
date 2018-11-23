package com.github.cimsbioko.server.service.impl;

public class BackupFailedEvent implements BackupServiceEvent {

    private final String backupName;
    private final Throwable cause;

    public BackupFailedEvent(String backupName, Throwable cause) {
        this.backupName = backupName;
        this.cause = cause;
    }

    public String getBackupName() {
        return backupName;
    }

    public Throwable getCause() {
        return cause;
    }
}
