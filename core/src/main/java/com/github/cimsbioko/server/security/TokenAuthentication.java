package com.github.cimsbioko.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

public class TokenAuthentication implements Authentication {

    private final Collection<? extends GrantedAuthority> authorities;
    private final String credentials;
    private final String details;
    private final String principal;
    private final boolean authenticated;
    private final boolean device;

    TokenAuthentication(String credentials) {
        this.authorities = null;
        this.credentials = credentials;
        this.details = null;
        this.principal = null;
        this.authenticated = false;
        this.device = false; // actually could be device, but this auth object is only a sentinel for filter
    }

    TokenAuthentication(String principal, String details, Collection<? extends GrantedAuthority> authorities, boolean device) {
        this.authorities = unmodifiableCollection(authorities);
        this.credentials = null;
        this.details = details;
        this.principal = principal;
        this.authenticated = true;
        this.device = device;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(authorities).orElse(emptyList());
    }

    @Override
    public String getCredentials() {
        return Optional.ofNullable(credentials).orElse("");
    }

    @Override
    public String getDetails() {
        return Optional.ofNullable(details).orElse("");
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getName() {
        return principal;
    }

    public boolean isDevice() {
        return device;
    }
}
