package com.github.cimsbioko.server.service.impl;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.scripting.DatabaseExport;
import com.github.cimsbioko.server.scripting.JsConfig;
import com.github.cimsbioko.server.service.MobileDbGenerator;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Implements mobile database generation support.
 */
public class MobileDbGeneratorImpl implements MobileDbGenerator {

    private static final Logger log = LoggerFactory.getLogger(MobileDbGeneratorImpl.class);

    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private File dataDir;

    private Exporter exporter;

    private JsConfig config;

    private ApplicationEventPublisher eventPublisher;

    public MobileDbGeneratorImpl(Exporter exporter, ApplicationEventPublisher eventPublisher) {
        this.exporter = exporter;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Order
    public void onCampaignUnload(CampaignUnloaded event) {
        log.info("unloading {}", event.getName());
        if (event.getName() == null) {
            config = null;
        }
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onCampaignLoad(CampaignLoaded event) {
        log.info("loading {}", event.getName());
        if (event.getName() == null) {
            config = event.getConfig();
        }
    }

    @Override
    public File getTarget() {
        return new File(dataDir, "cims-tablet.db");
    }

    @Resource(name = "dataDir")
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    @Async
    @Transactional
    public void generateMobileDb() {
        try {
            generateMobileDb(getTarget());
        } catch (IOException | SQLException | NoSuchAlgorithmException e) {
            log.error("failed to generate mobile db", e);
            eventPublisher.publishEvent(new MobileDbGeneratorFinished());
        }
    }

    @Transactional
    public void generateMobileDb(File dest) throws IOException, SQLException, NoSuchAlgorithmException {

        int tablesProcessed = 0;

        // do nothing if there is no config loaded
        if (config == null) {
            log.warn("aborting mobile db generation, no config");
            return;
        }

        DatabaseExport export = config.getDatabaseExport();

        // do nothing if there is no export defined in config (unlikely, since this is only scheduled from one)
        if (export == null) {
            log.warn("aborting mobile db generation, no export defined in config");
            return;
        }

        Map<String, String> tableQueries = export.exportQueries();

        eventPublisher.publishEvent(new MobileDbGeneratorStarted());

        File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
        File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
        File metaScratch = new File(dest.getParentFile(), metaDest.getName() + ".tmp");

        eventPublisher.publishEvent(new MobileDbGeneratorUpdate());

        // run each of the export's init scripts in order
        for (String initScriptName : export.initScripts()) {
            log.debug("executing init script {} on {}", initScriptName, scratch);
            exporter.scriptTarget(config.getResource(initScriptName), scratch);
        }

        // export each of the queries as a table in the target database file
        for (Map.Entry<String, String> e : tableQueries.entrySet()) {
            log.debug("executing query '{}' on {}", e.getValue(), scratch);
            exporter.export(e.getValue(), e.getKey(), scratch);
            eventPublisher.publishEvent(new MobileDbGeneratorUpdate(++tablesProcessed));
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
            log.info("successfully generated mobile db, content signature: {}", md5);
            eventPublisher.publishEvent(new MobileDbGeneratorFinished(md5));
        } else {
            eventPublisher.publishEvent(new MobileDbGeneratorFinished());
        }
    }
}

class MobileDbGeneratorStarted implements MobileDbGeneratorEvent {
}

class MobileDbGeneratorUpdate implements MobileDbGeneratorEvent {

    private final int tablesProcessed;

    MobileDbGeneratorUpdate() {
        this(0);
    }

    MobileDbGeneratorUpdate(int tablesProcessed) {
        this.tablesProcessed = tablesProcessed;
    }

    int getTablesProcessed() {
        return tablesProcessed;
    }
}

class MobileDbGeneratorFinished implements MobileDbGeneratorEvent {

    private final String contentHash;

    MobileDbGeneratorFinished() {
        this("generation failed - no content");
    }

    MobileDbGeneratorFinished(String contentHash) {
        this.contentHash = contentHash;
    }

    String getContentHash() {
        return contentHash;
    }
}