package com.github.cimsbioko.server.webapi.odk;

import javax.annotation.Resource;
import java.io.File;

public class DefaultFormFileSystem implements FormFileSystem {

    @Resource
    private File formsDir;

    @Override
    public String getFormFilePath(String id, String version) {
        return String.format("%s/%s.xml", getFormDirPath(id, version), id);
    }

    @Override
    public String getFormDirPath(String id, String version) {
        return String.format("%s/%s", id, version);
    }

    @Override
    public String getFormFilePath(String id, String version, String filename) {
        return String.format("%s/%s/%s/%s", formsDir, id, version, filename);
    }
}
