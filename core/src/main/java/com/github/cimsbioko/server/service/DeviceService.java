package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Device;

import java.util.Map;

public interface DeviceService {
    Map<String, String> completeRegistration(String deviceName, String description);
    String generateToken(String deviceName);
}
