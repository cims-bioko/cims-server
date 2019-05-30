package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.TokenRepository;
import com.github.cimsbioko.server.domain.Device;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

public class DeviceAuthenticationProvider implements AuthenticationProvider {

    private final DeviceRepository deviceRepo;
    private final TokenRepository tokenRepo;
    private final RoleMapper roleMapper;
    private final TokenHasher hasher;

    public DeviceAuthenticationProvider(DeviceRepository deviceRepo, TokenRepository tokenRepo, RoleMapper roleMapper, TokenHasher hasher) {
        this.deviceRepo = deviceRepo;
        this.roleMapper = roleMapper;
        this.hasher = hasher;
        this.tokenRepo = tokenRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        DeviceAuthentication auth = (DeviceAuthentication) authentication;
        Device device = tokenRepo.findById(hasher.hash(auth.getCredentials()))
                .flatMap(deviceRepo::findByToken)
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        return new DeviceAuthentication(device.getName(), device.getDescription(), roleMapper.rolesToAuthorities(device.getRoles()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return DeviceAuthentication.class.isAssignableFrom(authentication);
    }
}
