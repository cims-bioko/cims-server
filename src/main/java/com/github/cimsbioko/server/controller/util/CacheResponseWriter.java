package com.github.cimsbioko.server.controller.util;

import java.io.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import org.apache.commons.io.IOUtils;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;

public class CacheResponseWriter {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    public void writeResponse(String contentType, File fileToWrite, String contentHash, HttpServletResponse response) throws IOException {

        if (!fileToWrite.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(contentType);
        response.setHeader("Content-Length", String.valueOf(fileToWrite.length()));

        if (contentHash != null) {
            response.setHeader("ETag", contentHash);
        }

        response.setStatus(HttpServletResponse.SC_OK);

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileToWrite));
            IOUtils.copy(is, response.getOutputStream());
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    public void refactoredWriteResponse(String filename, HttpServletResponse response) throws IOException {

        InputStream inputStream = new FileInputStream(filename);
        ServletOutputStream outputStream = response.getOutputStream();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Length", String.valueOf(new File(filename).length()));

        byte[] buffer = new byte[8192];
        int bytesRead;
        int bytesBuffered = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            bytesBuffered += bytesRead;
            if (bytesBuffered > 1024 * 1024) { //flush after 1MB
                bytesBuffered = 0;
                outputStream.flush();
            }
        }

        outputStream.flush();

        IOUtils.closeQuietly(inputStream);

    }

}
