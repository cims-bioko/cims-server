package com.github.cimsbioko.server.webapi.odk;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultFormFileSystem implements FormFileSystem {

    @Resource
    private File formsDir;

    @Override
    public Path getXmlFormPath(String id, String version) {
        return getFormFilePath(id, version, id + ".xml");
    }

    @Override
    public Path getXlsformPath(String id, String version) {
        return getFormFilePath(id, version, id + ".xlsx");
    }

    @Override
    public Path getFormDirPath(String id, String version) {
        return formsDir.toPath().resolve(Paths.get(id, version));
    }

    public Path getFormFilePath(String id, String version, String filename) {
        return getFormDirPath(id, version).resolve(filename);
    }
}
