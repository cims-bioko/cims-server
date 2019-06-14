package com.github.cimsbioko.server.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A simple password encoder that used a simple one-way hash. This should not be used with low-entropy passwords.
 */
public class SHAPasswordEncoder implements PasswordEncoder {

    private final TokenHasher tokenHasher;

    public SHAPasswordEncoder(TokenHasher tokenHasher) {
        this.tokenHasher = tokenHasher;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return tokenHasher.hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
