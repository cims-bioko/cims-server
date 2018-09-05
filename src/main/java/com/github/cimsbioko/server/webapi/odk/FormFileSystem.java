package com.github.cimsbioko.server.webapi.odk;

import java.nio.file.Path;

public interface FormFileSystem {
    Path getXmlFormPath(String id, String version);
    Path getXlsformPath(String id, String version);
    Path getFormDirPath(String id, String version);
    Path getFormFilePath(String id, String version, String filename);
}
