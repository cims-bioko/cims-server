package com.github.cimsbioko.server.webapi.odk;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FileHasher {
    String hashFile(File file) throws IOException, NoSuchAlgorithmException;
}
