package com.github.cimsbioko.server.webapi.odk;

import java.io.File;

public interface SubmissionFileSystem {
    File getSubmissionDir(String instanceId);
    String getSubmissionFilePath(String idScheme, String id, String filename, String extension);
    String getSubmissionPath(String idScheme, String id);
}
