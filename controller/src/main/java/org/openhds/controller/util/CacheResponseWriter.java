package org.openhds.controller.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.PrivilegeConstants;

public class CacheResponseWriter {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    public void writeResponse(File fileToWrite, HttpServletResponse response) throws IOException {
        if (!fileToWrite.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        InputStream inputStream = new FileInputStream(fileToWrite);
        ServletOutputStream outputStream = response.getOutputStream();

        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        IOUtils.closeQuietly(inputStream);

    }

}
