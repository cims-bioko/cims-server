package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.domain.Device;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;

public class DeviceMapper {
    public UserDetails deviceToUserDetails(Device device) {
        String [] authorities = {"CREATE_DEVICE_TOKEN", "REGISTER_DEVICE"};
        return new org.springframework.security.core.userdetails.User(
                device.getName(),
                device.getSecret(),
                Arrays.stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .collect(toList()));
    }
}
