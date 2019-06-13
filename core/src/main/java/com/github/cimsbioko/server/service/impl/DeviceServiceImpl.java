package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.TokenRepository;
import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.security.TokenGenerator;
import com.github.cimsbioko.server.security.TokenHasher;
import com.github.cimsbioko.server.service.DeviceService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepo;
    private final TokenRepository tokenRepo;
    private final TokenGenerator tokenGen;
    private final TokenHasher tokenHasher;

    public DeviceServiceImpl(DeviceRepository deviceRepo, TokenRepository tokenRepo, TokenGenerator tokenGen, TokenHasher tokenHasher) {
        this.deviceRepo = deviceRepo;
        this.tokenRepo = tokenRepo;
        this.tokenGen = tokenGen;
        this.tokenHasher = tokenHasher;
    }

    @Transactional
    public Map<String, String> completeRegistration(String deviceName, String description) {
        Device device = deviceRepo.findByName(deviceName).orElseThrow(() -> new RuntimeException("device not found"));
        device.setDescription(description);
        String secret = tokenGen.generate();
        device.setSecret(tokenHasher.hash(secret));
        device.setSecretExpires(null);
        Map<String,String> result = new HashMap<>();
        result.put("secret", secret);
        result.put("access_token", generateToken(device.getName()));
        return result;
    }

    @Transactional
    public String generateToken(String deviceName) {
        Device device = deviceRepo.findByName(deviceName).orElseThrow(() -> new RuntimeException("device not found"));
        Optional.ofNullable(device.getToken())
                .ifPresent(token -> token.setExpires(Timestamp.from(Instant.now())));
        String token = tokenGen.generate();
        device.setToken(tokenRepo.save(new AccessToken(tokenHasher.hash(token))));
        deviceRepo.save(device);
        return token;
    }
}
