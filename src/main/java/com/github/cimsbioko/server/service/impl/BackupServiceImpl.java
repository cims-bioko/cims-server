package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.domain.Backup;
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
        callProcedure("{ call backup_data(current_schema(), ?, ?) }", name, description);
    }

    @Override
    @Transactional
    public void deleteBackup(String name) {
        callProcedure("{ call delete_backup(?) }", name);
    }

    @Override
    @Transactional
    public Backup updateBackup(String name, Backup updated) {
        callProcedure("{ call update_backup(?, ?, ?) }", name, updated.getName(), updated.getDescription());
        return em.find(Backup.class, updated.getName());
    }

    private void callProcedure(String call, String... args) {
        Session session = em.unwrap(Session.class);
        session.flush();
        session.doWork(c -> {
            try (CallableStatement stmt = c.prepareCall(call)) {
                for (int i = 0; i < args.length; i++) {
                    stmt.setString(i + 1, args[i]);
                }
                stmt.execute();
            }
        });
    }
}
