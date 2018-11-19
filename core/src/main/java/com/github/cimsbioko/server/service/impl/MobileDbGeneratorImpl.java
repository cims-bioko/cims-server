package com.github.cimsbioko.server.service.impl;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.service.MobileDbGenerator;
import com.github.cimsbioko.server.sqliteexport.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

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

    private Properties tableQueries;

    private org.springframework.core.io.Resource preDdl;

    private org.springframework.core.io.Resource postDdl;

    private ApplicationEventPublisher eventPublisher;

    public MobileDbGeneratorImpl(Exporter exporter, ApplicationEventPublisher eventPublisher) {
        this.exporter = exporter;
        tableQueries = new Properties();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public File getTarget() {
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

        eventPublisher.publishEvent(new MobileDbGeneratorStarted());

        File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
        File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
        File metaScratch = new File(dest.getParentFile(), metaDest.getName() + ".tmp");

        eventPublisher.publishEvent(new MobileDbGeneratorUpdate());
        // Export each of the queries as a table in the target database file
        exporter.scriptTarget(preDdl.getInputStream(), scratch);
        for (Map.Entry e : tableQueries.entrySet()) {
            exporter.export(e.getValue().toString(), e.getKey().toString(), scratch);
            eventPublisher.publishEvent(new MobileDbGeneratorUpdate(++tablesProcessed));
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