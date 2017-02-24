package com.github.cimsbioko.server.webapi;

import com.github.batkinson.jrsync.Metadata;
import com.github.cimsbioko.server.task.service.AsyncTaskService;
import com.github.cimsbioko.server.task.support.FileResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

import static com.github.cimsbioko.server.task.service.AsyncTaskService.MOBILEDB_TASK_NAME;


/**
 * Shared controller for all pre-generated cache files for tablet
 * synchronization. It uses content negotiation and awareness of sync metadata
 * to help tablets optimize bandwidth consumption:
 * <p>
 * <li>skip syncing when its local content is identical</li>
 * <li>use a zsync to efficiently synchronize with existing content</li>
 * <li>perform a full-download is syncing is not possible</li>
 */
@Controller
public class MobileDatabaseResource {

    public static final String MOBILEDB_PATH = "/mobiledb/cached";
    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";

    @Autowired
    private FileResolver fileResolver;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @RequestMapping(value = MOBILEDB_PATH, method = RequestMethod.GET, produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    public String mobileDB(WebRequest request) throws ServletException, IOException {

        String contentHash = asyncTaskService.getContentHash(MOBILEDB_TASK_NAME);
        File cacheFile = fileResolver.resolveMobileDBFile();
        File metadataFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + Metadata.FILE_EXT);
        String accept = request.getHeader(Headers.ACCEPT);

        if (request.checkNotModified(contentHash)) {
            return null;
        }

        if (accept != null && accept.contains(Metadata.MIME_TYPE) && metadataFile.exists()) {
            return "forward:/WEB-INF/cached-files/cims-tablet.db.jrsmd";
        }
        return "forward:/WEB-INF/cached-files/cims-tablet.db";
    }
}
