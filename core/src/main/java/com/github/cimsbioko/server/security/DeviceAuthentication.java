package com.github.cimsbioko.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

public class DeviceAuthentication implements Authentication {

    private final Collection<? extends GrantedAuthority> authorities;
    private final String credentials;
    private final String details;
    private final String device;
    private final boolean authenticated;

    DeviceAuthentication(String credentials) {
        this.authorities = null;
        this.credentials = credentials;
        this.details = null;
        this.device = null;
        this.authenticated = false;
    }

    DeviceAuthentication(String device, String details, Collection<? extends GrantedAuthority> authorities) {
        this.authorities = unmodifiableCollection(authorities);
        this.credentials = null;
        this.details = details;
        this.device = device;
        this.authenticated = true;
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
        return device;
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
        return device;
    }
}
