package com.github.cimsbioko.server.webapi.rest;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.service.SyncService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.cimsbioko.server.config.WebConfig.CACHED_FILES_PATH;


/**
 * Endpoint for tablet database synchronization. It uses content negotiation and awareness of sync metadata to help
 * tablets optimize bandwidth consumption:
 * <p>
 * <li>skip syncing when its local content is identical</li>
 * <li>use zsync to efficiently synchronize with existing content</li>
 * <li>perform a full-download if zsync is not possible</li>
 */
@Controller
public class MobileDatabaseResource {

    public static final String ACCEPT = "Accept";
    public static final String MOBILEDB_PATH = "/api/rest/mobiledb";
    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";
    public static final String MOBILEDB_EXPORT_PATH = "/api/rest/mobiledb/export";
    public static final String INSTALLABLE_FILENAME = "openhds.db";

    private SyncService service;

    public MobileDatabaseResource(SyncService service) {
        this.service = service;
    }

    @RequestMapping(value = MOBILEDB_PATH, method = RequestMethod.GET, produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    public String mobileDB(WebRequest request) {

        File cacheFile = service.getSyncFile();
        File metadataFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + Metadata.FILE_EXT);
        String accept = request.getHeader(ACCEPT);

        if (accept != null && accept.contains(Metadata.MIME_TYPE) && metadataFile.exists()) {
            return "forward:" + CACHED_FILES_PATH + "/" + metadataFile.getName();
        }
        return "forward:" + CACHED_FILES_PATH + "/" + cacheFile.getName();
    }

    @RequestMapping(value = MOBILEDB_EXPORT_PATH, method = RequestMethod.GET)
    public void browserExport(HttpServletResponse response) throws IOException {

        FileSystemResource dbFileRes = new FileSystemResource(service.getSyncFile());

        if (!dbFileRes.isReadable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unable to find mobiledb file. Try generating it from the tasks menu.");
        } else {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + INSTALLABLE_FILENAME + ".zip");
            try (ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream())) {
                ZipEntry e = new ZipEntry(INSTALLABLE_FILENAME);
                e.setSize(dbFileRes.contentLength());
                e.setTime(System.currentTimeMillis());
                zOut.putNextEntry(e);
                StreamUtils.copy(dbFileRes.getInputStream(), zOut);
                zOut.closeEntry();
                zOut.finish();
            }
        }
    }
}
