package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.DeviceRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.Timestamp;
import java.util.Optional;

import static java.time.Instant.now;

public class DeviceDetailsService implements UserDetailsService {

    private final DeviceRepository deviceRepo;
    private final DeviceMapper deviceMapper;

    public DeviceDetailsService(DeviceRepository deviceRepo, DeviceMapper deviceMapper) {
        this.deviceRepo = deviceRepo;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String deviceName) throws UsernameNotFoundException {
        return deviceRepo.findByName(deviceName)
                .filter(d -> Optional.ofNullable(d.getSecretExpires())
                        .map(e -> e.after(Timestamp.from(now())))
                        .orElse(true))
                .map(deviceMapper::deviceToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("device not found"));
    }
}
