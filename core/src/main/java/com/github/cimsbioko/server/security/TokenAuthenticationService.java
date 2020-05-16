package com.github.cimsbioko.server.security;

import org.springframework.security.core.Authentication;

public interface TokenAuthenticationService {
    Authentication authenticate(TokenAuthentication auth);
}
