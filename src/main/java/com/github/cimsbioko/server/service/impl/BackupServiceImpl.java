package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.BackupService;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.CallableStatement;

public class BackupServiceImpl implements BackupService {

    private EntityManager em;

    public BackupServiceImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Async
    @Transactional
    public void createBackup(String name, String description) {
        Session session = em.unwrap(Session.class);
        session.flush();
        session.doWork(c -> {
            try (CallableStatement f = c.prepareCall("{ call backup_data(current_schema(), ?, ?) }")) {
                f.setString(1, name);
                f.setString(2, description);
                f.execute();
            }
        });
    }
}
