package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.dao.*;
import com.github.cimsbioko.server.security.TokenGenerator;
import com.github.cimsbioko.server.security.TokenHasher;
import com.github.cimsbioko.server.service.*;
import com.github.cimsbioko.server.service.impl.*;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import com.github.cimsbioko.server.webapi.odk.FileHasher;
import com.github.cimsbioko.server.webapi.odk.FormFileSystem;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import javax.persistence.EntityManager;
import java.io.File;

@Configuration
public class ServiceConfig {

    @Bean
    public BackupService backupService(EntityManager em, ApplicationEventPublisher publisher) {
        return new BackupServiceImpl(em, publisher);
    }

    @Bean
    public ErrorService errorService(ErrorRepository repo) {
        return new ErrorServiceImpl(repo);
    }

    @Bean
    public FormService formService(FormRepository repo, FormSubmissionRepository submissionRepo, FileHasher hasher, FormFileSystem fs, XLSFormService xlsformService) {
        return new FormServiceImpl(repo, submissionRepo, hasher, fs, xlsformService);
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
    public SyncService syncService(TaskScheduler scheduler, CampaignRepository repo, File dataDir, Exporter exporter, ApplicationEventPublisher publisher) {
        return new SyncServiceImpl(scheduler, repo, dataDir, exporter, publisher);
    }

    @Bean
    public DeviceService deviceService(DeviceRepository deviceRepo, TokenRepository tokenRepo, TokenGenerator tokenGen, TokenHasher tokenHasher) {
        return new DeviceServiceImpl(deviceRepo, tokenRepo, tokenGen, tokenHasher);
    }

    @Bean
    public CampaignServiceImpl campaignService(CampaignRepository repo, File campaignsDir, ApplicationEventPublisher publisher) {
        return new CampaignServiceImpl(repo, campaignsDir, publisher);
    }

    @Bean
    public FormProcessorServiceImpl formProcessingService(CampaignRepository campaignRepository) {
        return new FormProcessorServiceImpl(campaignRepository);
    }

    @Bean
    ScheduledFormProcessing scheduledFormProcessing(FormSubmissionService formsService,
                                                    FormProcessorService formProcessorService,
                                                    ErrorService errorService) {
        return new ScheduledFormProcessing(formsService, formProcessorService, errorService);
    }

    @Bean
    public GeometryService geometryService(GeometryFactory geometryFactory) {
        return new GeometryServiceImpl(geometryFactory);
    }
}
