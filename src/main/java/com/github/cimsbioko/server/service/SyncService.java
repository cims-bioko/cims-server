package com.github.cimsbioko.server.service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public interface SyncService {
    File getSyncFile();
    void generateMobileDb(File dest) throws IOException, SQLException, NoSuchAlgorithmException;
}
