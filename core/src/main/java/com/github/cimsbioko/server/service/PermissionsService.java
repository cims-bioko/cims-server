package com.github.cimsbioko.server.service;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface PermissionsService {
    Collection<? extends GrantedAuthority> devicePermissions(String deviceUuid);
    Collection<? extends GrantedAuthority> userPermissions(String userUuid);
}
