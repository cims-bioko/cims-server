package com.github.cimsbioko.server.service;

import java.util.Map;

public interface DeviceService {
    Map<String, String> completeRegistration(String deviceName, String description);
    String generateToken(String deviceName);
}
