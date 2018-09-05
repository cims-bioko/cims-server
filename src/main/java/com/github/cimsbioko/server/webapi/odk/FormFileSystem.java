package com.github.cimsbioko.server.webapi.odk;

public interface FormFileSystem {
    String getFormFilePath(String id, String version);
    String getFormDirPath(String id, String version);
    String getFormFilePath(String id, String version, String filename);
}
