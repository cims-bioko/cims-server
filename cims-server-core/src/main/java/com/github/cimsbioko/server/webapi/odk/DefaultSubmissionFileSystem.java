package com.github.cimsbioko.server.webapi.odk;

import javax.annotation.Resource;
import java.io.File;

public class DefaultSubmissionFileSystem implements SubmissionFileSystem {

    @Resource
    private File submissionsDir;

    public File getSubmissionDir(String instanceId) {
        return new File(submissionsDir, instanceId.replaceFirst(":", "/"));
    }

    @Override
    public String getSubmissionFilePath(String idScheme, String id, String filename, String extension) {
        return String.format("%s/%s.%s", getSubmissionPath(idScheme, id), filename, extension);
    }

    public String getSubmissionPath(String idScheme, String id) {
        return String.format("%s/%s/%s", submissionsDir, idScheme, id);
    }

}
