package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.dao.*;
import com.github.cimsbioko.server.security.RoleMapper;
import com.github.cimsbioko.server.security.TokenGenerator;
import com.github.cimsbioko.server.security.TokenHasher;
import com.github.cimsbioko.server.service.*;
import com.github.cimsbioko.server.service.impl.*;
import com.github.cimsbioko.server.service.impl.backup.BackupServiceImpl;
import com.github.cimsbioko.server.service.impl.campaign.CampaignServiceImpl;
import com.github.cimsbioko.server.service.impl.indexing.IndexingServiceImpl;
import com.github.cimsbioko.server.service.impl.sync.SyncServiceImpl;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import com.github.cimsbioko.server.webapi.odk.FileHasher;
import com.github.cimsbioko.server.webapi.odk.FormFileSystem;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    public FormService formService(FormRepository repo, FormSubmissionRepository submissionRepo, FileHasher hasher,
                                   FormFileSystem fs, XLSFormService xlsformService, FormMetadataService metadataService) {
        return new FormServiceImpl(repo, submissionRepo, hasher, fs, xlsformService, metadataService);
    }

    @Bean
    public FormMetadataService metadataService(FormRepository formRepository) {
        return new FormMetadataServiceImpl(formRepository, new SchemaExtractorImpl());
    }

    @Bean
    public ScriptableFormMetadataService scriptableFormMetadataService(FormMetadataService metadataService) {
        return new ScriptableFormMetadataServiceImpl(metadataService);
    }

    @Bean
    public StoredProcService storedProcService(EntityManager em) {
        return new StoredProcServiceImpl(em);
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
    ScheduledFormProcessing scheduledFormProcessing(EntityManager entityManager, FormSubmissionService formsService,
                                                    FormProcessorService formProcessorService,
                                                    ErrorService errorService) {
        return new ScheduledFormProcessing(entityManager, formsService, formProcessorService, errorService);
    }

    @Bean
    public GeometryService geometryService(GeometryFactory geometryFactory) {
        return new GeometryServiceImpl(geometryFactory);
    }

    @Bean
    PermissionsService permissionsService(DeviceRepository deviceRepo, UserRepository userRepo, RoleMapper roleMapper) {
        return new PermissionsServiceImpl(deviceRepo, userRepo, roleMapper);
    }

    @Bean
    ExportSQLBuilder exportSQLBuilder(IdNamingStrategy idNamingStrategy, ParamNamingStrategy paramNamingStrategy) {
        return new ExportSQLBuilderImpl(idNamingStrategy, paramNamingStrategy);
    }

    @Bean
    SubmissionExportService submissionExportService(ExportSQLBuilder sqlBuilder, NamedParameterJdbcTemplate jdbcTemplate) {
        return new SubmissionExportServiceImpl(sqlBuilder, jdbcTemplate);
    }

    @Bean
    IndexingService indexingService(EntityManager em, ApplicationEventPublisher eventPublisher) {
        return new IndexingServiceImpl(em, eventPublisher);
    }

    @Bean
    ParamNamingStrategy paramNamingStrategy() {
        return new JdbcParamNamingStrategy();
    }

    @Bean
    IdNamingStrategy idNamingStrategy() {
        return new PostgresqlIdNamingStrategy();
    }
}
