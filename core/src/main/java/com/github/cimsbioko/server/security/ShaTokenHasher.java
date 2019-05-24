package com.github.cimsbioko.server.security;


import org.apache.commons.codec.digest.DigestUtils;

public class ShaTokenHasher implements TokenHasher {
    @Override
    public String hash(String token) {
        return DigestUtils.sha1Hex(token);
    }
}
