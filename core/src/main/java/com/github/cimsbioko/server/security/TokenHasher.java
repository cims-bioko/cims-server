package com.github.cimsbioko.server.security;

public interface TokenHasher {
    String hash(String token);
}
