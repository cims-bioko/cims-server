package com.github.cimsbioko.server.service.impl.sync;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.scripting.DatabaseExport;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.SyncService;
import com.github.cimsbioko.server.service.impl.campaign.CampaignLoadedEvent;
import com.github.cimsbioko.server.service.impl.campaign.CampaignUnloadedEvent;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.github.cimsbioko.server.util.TimeUtil.describeDuration;
import static org.apache.commons.codec.binary.Hex.encodeHexString;


public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private final TaskScheduler scheduler;

    private final File dataDir;

    private final Map<String, SyncTask> campaignTasks = new ConcurrentHashMap<>();

    private final Exporter exporter;

    private final ApplicationEventPublisher eventPublisher;

    private final CampaignRepository repo;

    public SyncServiceImpl(TaskScheduler scheduler, CampaignRepository repo, File dataDir, Exporter exporter,
                           ApplicationEventPublisher eventPublisher) {
        this.scheduler = scheduler;
        this.dataDir = dataDir;
        this.exporter = exporter;
        this.eventPublisher = eventPublisher;
        this.repo = repo;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onCampaignUnload(CampaignUnloadedEvent event) {
        String campaignUuid = event.getUuid();
        Optional.ofNullable(campaignTasks.get(campaignUuid))
                .ifPresent(task -> {
                    task.cancel();
                    campaignTasks.remove(campaignUuid);
                    log.info("removed db export for campaign '{}' ({})", event.getName(), campaignUuid);
                });
    }

    @EventListener
    @Order
    public void onCampaignLoad(CampaignLoadedEvent event) {
        String campaignUuid = event.getUuid();
        JsConfig config = event.getConfig();
        String campaignName = event.getName();
        scheduleExport(campaignUuid, campaignName, config);
    }

    private void scheduleExport(String campaignUuid, String campaignName, JsConfig config) {
        Optional
                .ofNullable(config.getDatabaseExport())
                .map(DatabaseExport::exportSchedule)
                .map(CronTrigger::new)
                .map(t -> scheduler.schedule(() -> requestExport(campaignUuid), t))
                .ifPresent(future -> {
                    campaignTasks.put(campaignUuid, new SyncTask(config, campaignUuid, campaignName, future));
                    log.info("added db export for campaign '{}' ({}), schedule '{}'",
                            campaignName, campaignUuid, config.getDatabaseExport().exportSchedule());
                });
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onExportStarted(ExportStartedEvent e) {
        SyncTask t = campaignTasks.get(e.getCampaignUuid());
        t.setStarted(new Date());
        t.setFinished(null);
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onExportUpdate(ExportStatusEvent e) {
        campaignTasks.get(e.getCampaignUuid()).setItemCount(e.getTablesProcessed());
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onExportFinished(ExportFinishedEvent e) {
        SyncTask t = campaignTasks.get(e.getCampaignUuid());
        t.setFinished(new Date());
        t.setDescriptor(e.getContentHash());
    }

    public Optional<Task> getTask(String campaign) {
        return Optional.ofNullable(campaignTasks.get(campaign));
    }

    @Override
    public void pauseSync(String campaign) {
        Optional.ofNullable(campaignTasks.get(campaign)).ifPresent(SyncTask::cancel);
    }

    @Override
    public void resumeSync(String campaign) {
        Optional.ofNullable(campaignTasks.get(campaign))
                .filter(task -> task.getStatus() == Status.PAUSED)
                .ifPresent(task -> scheduleExport(task.getCampaignUuid(), task.getCampaignName(), task.getConfig()));
    }

    @Override
    public Status getStatus(String campaign) {
        return Optional.ofNullable(campaignTasks.get(campaign)).map(SyncTask::getStatus).orElse(Status.NO_SYNC);
    }

    public File getOutput(String campaign) {
        return new File(dataDir, String.format("%s.db", campaign));
    }

    @Async
    @Transactional
    @Override
    public void requestExport(String campaign) {
        try {
            Optional<Campaign> optionalActiveCampaign = repo.findActiveByUuid(campaign);
            if (optionalActiveCampaign.isPresent()) {
                runExport(optionalActiveCampaign.get());
            }
        } catch (IOException | SQLException | NoSuchAlgorithmException e) {
            log.error("failed to generate mobile db for campaign " + campaign, e);
            eventPublisher.publishEvent(new ExportFinishedEvent(campaign));
        }
    }

    private void runExport(Campaign campaign) throws IOException, SQLException, NoSuchAlgorithmException {

        long start = System.currentTimeMillis();

        String campaignUuid = campaign.getUuid();

        int tablesProcessed = 0;

        JsConfig config = campaignTasks.get(campaignUuid).getConfig();
        File dest = getOutput(campaignUuid);
        DatabaseExport export = config.getDatabaseExport();

        Map<String, String> tableQueries = export.exportQueries();

        eventPublisher.publishEvent(new ExportStartedEvent(campaignUuid));

        File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
        File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
        File metaScratch = new File(dest.getParentFile(), metaDest.getName() + ".tmp");

        eventPublisher.publishEvent(new ExportStatusEvent(campaignUuid));

        // run each of the export's init scripts in order
        for (String initScriptName : export.initScripts()) {
            log.debug("executing init script {} on {}", initScriptName, scratch);
            exporter.scriptTarget(config.getResource(initScriptName), scratch);
        }

        // export each of the queries as a table in the target database file
        for (Map.Entry<String, String> e : tableQueries.entrySet()) {
            log.debug("executing query '{}' on {}", e.getValue(), scratch);
            exporter.export(e.getValue(), e.getKey(), scratch);
            tablesProcessed += 1;
            eventPublisher.publishEvent(new ExportStatusEvent(campaignUuid, tablesProcessed,
                    (int) ((tablesProcessed / (float) tableQueries.size()) * 100)));
        }

        // run each of the export's post scripts in order
        for (String postScriptName : export.postScripts()) {
            log.debug("executing post script {} on {}", postScriptName, scratch);
            exporter.scriptTarget(config.getResource(postScriptName), scratch);
        }

        log.debug("generating sync metadata");
        try (InputStream in = new FileInputStream(scratch)) {
            Metadata.generate("", DEFAULT_SYNC_BLOCK_SIZE, MD5, MD5, in, metaScratch);
        }
        String md5;
        try (DataInputStream metaStream = new DataInputStream(new FileInputStream(metaScratch))) {
            md5 = encodeHexString(Metadata.read(metaStream).getFileHash());
        }

        // Complete the process, latching the new file contents and sync metadata
        if (scratch.renameTo(dest) && metaScratch.renameTo(metaDest)) {
            log.info("exported {} for campaign '{}' with signature: {} in {}",
                    dest.getName(), campaign.getName(), md5, describeDuration(System.currentTimeMillis() - start));
            eventPublisher.publishEvent(new ExportFinishedEvent(campaignUuid, md5));
        } else {
            eventPublisher.publishEvent(new ExportFinishedEvent(campaignUuid));
        }
    }

    static class SyncTask implements SyncService.Task {

        private final JsConfig config;
        private final String campaignUuid, campaignName;
        private final ScheduledFuture<?> taskFuture;
        private Date started, finished;
        private int itemCount;
        private String descriptor;

        SyncTask(JsConfig config, String campaignUuid, String campaignName, ScheduledFuture<?> future) {
            this.config = config;
            this.campaignName = campaignName;
            this.campaignUuid = campaignUuid;
            this.taskFuture = future;
        }

        JsConfig getConfig() {
            return config;
        }

        String getCampaignUuid() {
            return campaignUuid;
        }

        String getCampaignName() {
            return campaignName;
        }

        public long getNextRunMinutes() {
            return taskFuture.getDelay(TimeUnit.MINUTES);
        }

        void cancel() {
            taskFuture.cancel(false);
        }

        void setStarted(Date started) {
            this.started = started;
        }

        void setFinished(Date finished) {
            this.finished = finished;
        }

        void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public Status getStatus() {
            if (started != null && finished == null) {
                return Status.RUNNING;
            } else if (taskFuture.isCancelled()) {
                return Status.PAUSED;
            } else {
                return Status.SCHEDULED;
            }
        }

        @Override
        public int getPercentComplete() {
            return (int) ((itemCount / (float) config.getDatabaseExport().exportQueries().size()) * 100);
        }

        public String getContentHash() {
            return descriptor;
        }

        void setDescriptor(String descriptor) {
            this.descriptor = descriptor;
        }
    }
}
