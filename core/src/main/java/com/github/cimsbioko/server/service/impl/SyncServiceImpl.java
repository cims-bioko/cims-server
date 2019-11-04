package com.github.cimsbioko.server.service.impl;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.domain.Task;
import com.github.cimsbioko.server.scripting.DatabaseExport;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.SyncService;
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
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.apache.commons.codec.binary.Hex.encodeHexString;


public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private final TaskScheduler scheduler;

    private final File dataDir;

    private final Map<String, SyncTask> campaignTasks = new HashMap<>();

    private Exporter exporter;

    private ApplicationEventPublisher eventPublisher;

    public SyncServiceImpl(TaskScheduler scheduler, File dataDir, Exporter exporter, ApplicationEventPublisher eventPublisher) {
        this.scheduler = scheduler;
        this.dataDir = dataDir;
        this.exporter = exporter;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onCampaignUnload(CampaignUnloaded event) {
        String campaign = event.getName();
        log.info("unloading campaign '{}'", campaign);
        Optional.ofNullable(campaignTasks.get(campaign))
                .ifPresent(task -> {
                    task.cancel();
                    campaignTasks.remove(campaign);
                });
    }

    @EventListener
    @Order
    public void onCampaignLoad(CampaignLoaded event) {
        String campaign = event.getName();
        log.info("loading campaign '{}'", campaign);
        JsConfig config = event.getConfig();
        Optional
                .ofNullable(config.getDatabaseExport())
                .map(DatabaseExport::exportSchedule)
                .map(CronTrigger::new)
                .map(t -> scheduler.schedule(() -> { requestExport(campaign); }, t))
                .ifPresent(future -> campaignTasks.put(campaign, new SyncTask(config, future)));
    }

    @EventListener
    public void onExportStarted(ExportStarted e) {
        SyncTask t = campaignTasks.get(e.getCampaign());
        t.setStarted(new Date());
        t.setFinished(null);
    }

    @EventListener
    public void onExportUpdate(ExportStatus e) {
        campaignTasks.get(e.getCampaign()).setItemCount(e.getTablesProcessed());
    }

    @EventListener
    public void onExportFinished(ExportFinished e) {
        SyncTask t = campaignTasks.get(e.getCampaign());
        t.setFinished(new Date());
        t.setDescriptor(e.getContentHash());
    }

    public Task getTask(String campaign) {
        return campaignTasks.get(campaign);
    }

    public File getOutput(String campaign) {
        return new File(dataDir, String.format("%s.db", campaign));
    }

    @Async
    @Transactional
    @Override
    public void requestExport(String campaign) {
        try {
            runExport(campaign);
        }  catch (IOException | SQLException | NoSuchAlgorithmException e) {
            log.error("failed to generate mobile db for campaign " + campaign, e);
            eventPublisher.publishEvent(new ExportFinished(campaign));
        }
    }

    private void runExport(String campaign) throws IOException, SQLException, NoSuchAlgorithmException {

        int tablesProcessed = 0;

        JsConfig config = campaignTasks.get(campaign).getConfig();
        File dest = getOutput(campaign);
        DatabaseExport export = config.getDatabaseExport();

        Map<String, String> tableQueries = export.exportQueries();

        eventPublisher.publishEvent(new ExportStarted(campaign));

        File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
        File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
        File metaScratch = new File(dest.getParentFile(), metaDest.getName() + ".tmp");

        eventPublisher.publishEvent(new ExportStatus(campaign));

        // run each of the export's init scripts in order
        for (String initScriptName : export.initScripts()) {
            log.debug("executing init script {} on {}", initScriptName, scratch);
            exporter.scriptTarget(config.getResource(initScriptName), scratch);
        }

        // export each of the queries as a table in the target database file
        for (Map.Entry<String, String> e : tableQueries.entrySet()) {
            log.debug("executing query '{}' on {}", e.getValue(), scratch);
            exporter.export(e.getValue(), e.getKey(), scratch);
            eventPublisher.publishEvent(new ExportStatus(campaign, ++tablesProcessed));
        }

        // run each of the export's post scripts in order
        for (String postScriptName : export.postScripts()) {
            log.debug("executing post script {} on {}", postScriptName, scratch);
            exporter.scriptTarget(config.getResource(postScriptName), scratch);
        }

        // Generate sync metadata
        try (InputStream in = new FileInputStream(scratch)) {
            Metadata.generate("", DEFAULT_SYNC_BLOCK_SIZE, MD5, MD5, in, metaScratch);
        }
        String md5;
        try (DataInputStream metaStream = new DataInputStream(new FileInputStream(metaScratch))) {
            md5 = encodeHexString(Metadata.read(metaStream).getFileHash());
        }

        // Complete the process, latching the new file contents and sync metadata
        if (scratch.renameTo(dest) && metaScratch.renameTo(metaDest)) {
            log.info("successfully generated {}, content signature: {}", dest.getName(), md5);
            eventPublisher.publishEvent(new ExportFinished(campaign, md5));
        } else {
            eventPublisher.publishEvent(new ExportFinished(campaign));
        }
    }

    private static class SyncTask implements Task {

        private final JsConfig config;
        private final ScheduledFuture taskFuture;
        private Date started, finished;
        private int itemCount;
        private String descriptor;

        SyncTask(JsConfig config, ScheduledFuture future) {
            this.config = config;
            this.taskFuture = future;
        }

        public JsConfig getConfig() {
            return config;
        }

        public ScheduledFuture getTaskFuture() {
            return taskFuture;
        }

        void cancel() {
            Optional.ofNullable(taskFuture).ifPresent(t -> t.cancel(false));
        }

        @Override
        public Date getStarted() {
            return started;
        }

        void setStarted(Date started) {
            this.started = started;
        }

        @Override
        public Date getFinished() {
            return finished;
        }

        void setFinished(Date finished) {
            this.finished = finished;
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public String getDescriptor() {
            return descriptor;
        }

        void setDescriptor(String descriptor) {
            this.descriptor = descriptor;
        }
    }
}

abstract class AbstractExportEvent implements MobileDbGeneratorEvent {

    private final String name;

    AbstractExportEvent(String campaign) {
        this.name = campaign;
    }

    @Override
    public String getCampaign() {
        return name;
    }
}

class ExportStarted extends AbstractExportEvent {

    ExportStarted(String campaign) {
        super(campaign);
    }
}

class ExportStatus extends AbstractExportEvent {

    private final int tablesProcessed;

    ExportStatus(String campaign) {
        this(campaign, 0);
    }

    ExportStatus(String campaign, int tablesProcessed) {
        super(campaign);
        this.tablesProcessed = tablesProcessed;
    }

    int getTablesProcessed() {
        return tablesProcessed;
    }
}

class ExportFinished extends AbstractExportEvent {

    private final String contentHash;

    ExportFinished(String campaign) {
        this(campaign, "generation failed - no content");
    }

    ExportFinished(String campaign, String contentHash) {
        super(campaign);
        this.contentHash = contentHash;
    }

    String getContentHash() {
        return contentHash;
    }
}

