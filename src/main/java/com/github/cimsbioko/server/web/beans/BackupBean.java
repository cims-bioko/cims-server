package com.github.cimsbioko.server.web.beans;

import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import com.github.cimsbioko.server.web.service.JsfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackupBean {

    private static final Logger log = LoggerFactory.getLogger(TaskBean.class);

    private BackupService service;
    private JsfService jsf;

    private String name;
    private String description;

    public BackupBean(BackupService service, JsfService jsf) {
        this.service = service;
        this.jsf = jsf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void reset() {
        setName("");
        setDescription("");
    }

    public void createBackup() {
        log.info("attempting to backup data to schema: {}", name);
        service.createBackup(name, description);
        reset();
        jsf.addMessage("backupScheduled", name);
    }

    public List<Backup> getBackups() {
        return service.getBackups();
    }
}
