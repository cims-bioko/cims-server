package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.security.RoleMapper;
import com.github.cimsbioko.server.service.PermissionsService;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class PermissionsServiceImpl implements PermissionsService {

    private final DeviceRepository deviceRepo;
    private final UserRepository userRepo;
    private final RoleMapper mapper;

    public PermissionsServiceImpl(DeviceRepository deviceRepo, UserRepository userRepo, RoleMapper mapper) {
        this.deviceRepo = deviceRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @Override
    public Collection<? extends GrantedAuthority> devicePermissions(String deviceUuid) {
        return deviceRepo
                .findById(deviceUuid)
                .map(Device::getRoles)
                .map(mapper::rolesToAuthorities)
                .orElse(Collections.emptySet());
    }

    @Override
    public Collection<? extends GrantedAuthority> userPermissions(String userUuid) {
        return userRepo
                .findById(userUuid)
                .map(User::getRoles)
                .map(mapper::rolesToAuthorities)
                .orElse(Collections.emptySet());
    }
}
