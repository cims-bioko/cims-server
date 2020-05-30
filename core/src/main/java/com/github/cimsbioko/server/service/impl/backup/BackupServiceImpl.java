package com.github.cimsbioko.server.service.impl.backup;

import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import com.github.cimsbioko.server.service.impl.backup.BackupCreatedEvent;
import com.github.cimsbioko.server.service.impl.backup.BackupFailedEvent;
import org.hibernate.Session;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.CallableStatement;

public class BackupServiceImpl implements BackupService {

    private final EntityManager em;

    private final ApplicationEventPublisher eventPublisher;

    public BackupServiceImpl(EntityManager em, ApplicationEventPublisher publisher) {
        this.em = em;
        this.eventPublisher = publisher;
    }

    @Override
    @Async
    @Transactional
    public void createBackup(String name, String description) {
        try {
            callProcedure("{ call backup_data(current_schema(), ?, ?) }", name, description);
            eventPublisher.publishEvent(new BackupCreatedEvent(name));
        } catch (Exception cause) {
            eventPublisher.publishEvent(new BackupFailedEvent(name, cause));
        }
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
