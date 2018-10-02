package com.github.cimsbioko.server.service.impl;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.dao.TaskRepository;
import com.github.cimsbioko.server.domain.Task;
import com.github.cimsbioko.server.service.SyncService;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Implements mobile database generation support.
 */
public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private static final String TASK_NAME = "Mobile DB Task";
    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private File dataDir;

    private TaskRepository taskRepo;

    private Exporter exporter;

    private Properties tableQueries;

    private org.springframework.core.io.Resource preDdl;

    private org.springframework.core.io.Resource postDdl;

    private TaskScheduler scheduler;

    private String schedule;

    private ScheduledFuture<?> scheduledTask;

    public SyncServiceImpl(TaskRepository taskRepo, Exporter exporter, TaskScheduler scheduler, String schedule) {
        this.taskRepo = taskRepo;
        this.exporter = exporter;
        tableQueries = new Properties();
        this.scheduler = scheduler;
        this.schedule = schedule;
    }

    @Override
    public File getSyncFile() {
        return new File(dataDir, "cims-tablet.db");
    }

    @Resource(name = "dataDir")
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    @Resource(name = "exportQueries")
    public void setTableQueries(Properties tableQueries) {
        this.tableQueries = tableQueries;
    }

    @Value("classpath:/pre-export.sql")
    public void setPreDdl(org.springframework.core.io.Resource scriptStream) {
        preDdl = scriptStream;
    }

    @Value("classpath:/post-export.sql")
    public void setPostDdl(org.springframework.core.io.Resource scriptStream) {
        postDdl = scriptStream;
    }

    public void generateMobileDb() {
        try {
            generateMobileDb(getSyncFile());
        } catch (IOException | NoSuchAlgorithmException | SQLException e) {
            log.error("failed to generate mobile db", e);
        }
    }

    @PostConstruct
    public void scheduleTask() {
        log.info("scheduling mobile database export task, schedule {}", schedule);
        scheduledTask = Optional.ofNullable(schedule)
                .filter(s -> !s.trim().isEmpty())
                .map(CronTrigger::new)
                .map(t -> scheduler.schedule(this::generateMobileDb, t))
                .orElse(null);
    }

    public boolean cancelTask() {
        log.info("canceling mobile database export task");
        return Optional.of(scheduledTask)
                .map(task -> task.cancel(false))
                .orElse(false);
    }

    public Optional<Long> getMinutesToNextRun() {
        return Optional.ofNullable(scheduledTask)
                .map(t -> t.getDelay(MINUTES));
    }

    @Transactional(readOnly = true)
    public Task getTask() {
        return taskRepo.findByName(TASK_NAME);
    }

    @Override
    @Transactional
    public void generateMobileDb(File dest) throws IOException, SQLException, NoSuchAlgorithmException {

        Task task = getTask();
        task.setStarted(Calendar.getInstance());
        task = taskRepo.save(task);

        File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
        File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
        File metaScratch = new File(dest.getParentFile(), metaDest.getName() + ".tmp");

        // Export each of the queries as a table in the target database file
        exporter.scriptTarget(preDdl.getInputStream(), scratch);
        for (Map.Entry e : tableQueries.entrySet()) {
            exporter.export(e.getValue().toString(), e.getKey().toString(), scratch);
        }
        exporter.scriptTarget(postDdl.getInputStream(), scratch);

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
            log.info("successfully generated mobile db, content signature: {}", md5);
            task.setDescriptor(md5);
            task.setFinished(Calendar.getInstance());
            task.setItemCount(tableQueries.size());
        }
    }
}
