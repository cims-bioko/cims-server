package com.github.cimsbioko.server.task.sqlite;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.task.SyncFileTask;
import com.github.cimsbioko.server.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Exports an sqlite database usable by the tablet application.
 */
@Component("mobileDBWriter")
public class MobileDBExportTask implements SyncFileTask {

    private static final Logger log = LoggerFactory.getLogger(MobileDBExportTask.class);
    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private Exporter exporter;

    private Properties tableQueries;

    private org.springframework.core.io.Resource preDdl;

    private org.springframework.core.io.Resource postDdl;

    @Autowired
    public MobileDBExportTask(Exporter exporter) {
        this.exporter = exporter;
        tableQueries = new Properties();
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

    @Override
    @Async
    @Transactional
    public void run(TaskContext context) {

        int tablesExported = 0;

        try {
            File dest = context.getDestinationFile();
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
            }
        } catch (IOException | SQLException | NoSuchAlgorithmException e) {
            log.error("failed to export mobile db", e);
        }
    }

}
