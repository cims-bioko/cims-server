package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.util.List;

@Component
public class BackupServiceImpl implements BackupService {

    private SessionFactory sessFact;

    @Autowired
    public BackupServiceImpl(SessionFactory sessionFactory) {
        this.sessFact = sessionFactory;
    }


    @Override
    @Async
    @Transactional
    public void createBackup(String name, String description) {
        sessFact.getCurrentSession().doWork(c -> {
            try (CallableStatement f = c.prepareCall("{ call backup_data(current_schema(), ?, ?) }")) {
                f.setString(1, name);
                f.setString(2, description);
                f.execute();
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Backup> getBackups() {
        return sessFact.getCurrentSession()
                .createQuery("select b from Backup b order by created desc", Backup.class)
                .getResultList();
    }
}
