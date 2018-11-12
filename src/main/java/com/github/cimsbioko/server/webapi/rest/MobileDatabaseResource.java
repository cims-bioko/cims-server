package com.github.cimsbioko.server.webapi.rest;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.service.MobileDbGenerator;
import com.github.cimsbioko.server.service.SyncService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import java.io.File;

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

    private static final String ACCEPT = "Accept";
    static final String MOBILEDB_PATH = "/api/rest/mobiledb";
    static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    private SyncService service;

    public MobileDatabaseResource(SyncService service) {
        this.service = service;
    }

    @RequestMapping(value = MOBILEDB_PATH, method = RequestMethod.GET, produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    public String mobileDB(WebRequest request) {

        File cacheFile = service.getOutput();
        File metadataFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + Metadata.FILE_EXT);
        String accept = request.getHeader(ACCEPT);

        if (request.checkNotModified(service.getTask().getDescriptor())) {
            return null;
        }

        if (accept != null && accept.contains(Metadata.MIME_TYPE) && metadataFile.exists()) {
            return "forward:" + CACHED_FILES_PATH + "/" + metadataFile.getName();
        }
        return "forward:" + CACHED_FILES_PATH + "/" + cacheFile.getName();
    }
}
