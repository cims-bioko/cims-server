package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.dao.ErrorRepository;
import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.dao.FormSubmissionRepository;
import com.github.cimsbioko.server.dao.LocationHierarchyRepository;
import com.github.cimsbioko.server.service.*;
import com.github.cimsbioko.server.service.impl.*;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import com.github.cimsbioko.server.webapi.odk.FileHasher;
import com.github.cimsbioko.server.webapi.odk.FormFileSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class ServiceConfig {

    @Bean
    public BackupService backupService(EntityManager em) {
        return new BackupServiceImpl(em);
    }

    @Bean
    public ErrorService errorService(ErrorRepository repo) {
        return new ErrorServiceImpl(repo);
    }

    @Bean
    public FormService formService(FormRepository repo, FileHasher hasher, FormFileSystem fs, XLSFormService xlsformService) {
        return new FormServiceImpl(repo, hasher, fs, xlsformService);
    }

    @Bean
    public LocationHierarchyService locationHierarchyService(EntityManager em, LocationHierarchyRepository repo) {
        return new LocationHierarchyServiceImpl(em, repo);
    }

    @Bean
    public FormSubmissionService submissionService(FormSubmissionRepository submissionRepo, FormRepository formRepo) {
        return new FormSubmissionServiceImpl(submissionRepo, formRepo);
    }

    @Bean
    public SyncService syncService(Exporter sqliteExporter) {
        return new SyncServiceImpl(sqliteExporter);
    }
}
