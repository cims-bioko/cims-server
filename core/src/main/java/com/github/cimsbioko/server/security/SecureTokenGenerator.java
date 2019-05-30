package com.github.cimsbioko.server.security;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;


/**
 * Generates random tokens encoded in RFC 4648 base-32. The reasoning is that the format is relatively ergonomic since
 * it uses only capital letters and digits. It is less resistant to transcription errors than other human-readable
 * formats since look-alike characters are not included in the alphabet. It is also more space efficient than
 * hex-encoded values.
 */
public class SecureTokenGenerator implements TokenGenerator {

    private final SecureRandom random;
    private final Base32 encoder;
    private final int bytes;

    public SecureTokenGenerator() {
        this(16);
    }

    private SecureTokenGenerator(int length) {
        if (length % 8 != 0) {
            throw new IllegalArgumentException("length should be a multiple of 8");
        }
        this.bytes = (length / 8) * 5;
        this.random = new SecureRandom();
        encoder = new Base32();
    }

    @Override
    public String generate() {
        byte[] value = new byte[bytes];
        random.nextBytes(value);
        return encoder.encodeAsString(value);
    }
}
