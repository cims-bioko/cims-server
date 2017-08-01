package org.openhds.webservice.resources;

import com.github.batkinson.jrsync.Metadata;

import org.openhds.task.service.AsyncTaskService;
import org.openhds.task.support.FileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import static org.openhds.task.service.AsyncTaskService.MOBILEDB_TASK_NAME;


/**
 * Shared controller for all pre-generated cache files for tablet
 * synchronization. It uses content negotiation and awareness of sync metadata
 * to help tablets optimize bandwidth consumption:
 *
 *   <li>skip syncing when its local content is identical</li>
 *   <li>use a zsync to efficiently synchronize with existing content</li>
 *   <li>perform a full-download is syncing is not possible</li>
 */
@Controller
public class CacheFileResource implements ServletContextAware {

    public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";
    public static final String MOBILEDB_FILENAME = "openhds.db";

    private static Logger log = LoggerFactory.getLogger(CacheFileResource.class);

    private ServletContext ctx;

    @Autowired
    private FileResolver fileResolver;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Override
    public void setServletContext(ServletContext servletContext) {
        ctx = servletContext;
    }

    /**
     * Allows us to set the ETag and forward to another servlet without it
     * overwriting it.
     */
    private static class OverrideHeaderResponse extends HttpServletResponseWrapper {

        Set<String> overridden = new HashSet<>();

        public OverrideHeaderResponse(HttpServletResponse response) {
            super(response);
        }

        public void fixHeader(String name, String value) {
            overridden.add(name.toLowerCase());
            super.setHeader(name, value);
        }

        public void setHeader(String name, String value) {
            if (!overridden.contains(name.toLowerCase())) {
                super.setHeader(name, value);
            }
        }
    }

    private String contextPath(File file) {
        return "/" + file.getAbsolutePath().replaceFirst(ctx.getRealPath("/"), "");
    }

    private void serviceTask(String taskName, HttpServletRequest request, HttpServletResponse response, String contentType)
            throws ServletException, IOException {

        try {
            String contentHash = asyncTaskService.getContentHash(taskName);
            String eTag = request.getHeader(Headers.IF_NONE_MATCH);

            if (eTag != null && eTag.equals(contentHash)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            OverrideHeaderResponse overrideResponse = new OverrideHeaderResponse(response);
            response = overrideResponse;

            if (contentHash != null) {
                overrideResponse.fixHeader(Headers.ETAG, contentHash);
            }

            if (contentType != null) {
                overrideResponse.fixHeader(Headers.CONTENT_TYPE, contentType);
            }

            File cacheFile = fileResolver.getFileForTask(taskName);
            File metadataFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + Metadata.FILE_EXT);

            String accept = request.getHeader(Headers.ACCEPT);
            if (accept != null && accept.contains(Metadata.MIME_TYPE) && metadataFile.exists()) {
                response.setContentType(Metadata.MIME_TYPE);
                request.getRequestDispatcher(contextPath(metadataFile)).forward(request, response);
                return;
            }

            request.getRequestDispatcher(contextPath(cacheFile)).forward(request, response);

        } catch (Exception e) {
            log.error("problem servicing request" + e.getMessage());
        }
    }

    @RequestMapping(value = "/mobiledb/cached", method = RequestMethod.GET, produces = {SQLITE_MIME_TYPE, Metadata.MIME_TYPE})
    public void mobileDB(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        serviceTask(MOBILEDB_TASK_NAME, request, response, SQLITE_MIME_TYPE);
    }

    @RequestMapping(value = "/mobiledb/export", method = RequestMethod.GET)
    public void browserExport(HttpServletResponse response) throws IOException {

        File dbFile = fileResolver.getFileForTask(MOBILEDB_TASK_NAME);
        FileSystemResource dbFileRes = new FileSystemResource(dbFile);

        if (!dbFileRes.isReadable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unable to find mobiledb file. Try generating it from the tasks menu.");
        } else {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + MOBILEDB_FILENAME + ".zip");
            try (ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream())) {
                ZipEntry e = new ZipEntry(MOBILEDB_FILENAME);
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
