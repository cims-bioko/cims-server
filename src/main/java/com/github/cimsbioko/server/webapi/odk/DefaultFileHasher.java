package com.github.cimsbioko.server.webapi.odk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;

public class DefaultFileHasher implements FileHasher {

    private static final int BLOCK_SIZE = 8192;

    @Override
    public String hashFile(File file) throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream in = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(MD5))) {
            byte[] buf = new byte[BLOCK_SIZE];
            while (in.read(buf) >= 0) {
                // reading only to compute hash
            }
            return Constants.MD5_SCHEME + encodeHexString(in.getMessageDigest().digest());
        }
    }
}
