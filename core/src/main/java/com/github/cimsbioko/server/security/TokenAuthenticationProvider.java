package com.github.cimsbioko.server.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class TokenAuthenticationProvider implements AuthenticationProvider {

    private final TokenAuthenticationService authenticationService;

    public TokenAuthenticationProvider(TokenAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        TokenAuthentication auth = (TokenAuthentication) authentication;
        return authenticationService.authenticate(auth);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}
