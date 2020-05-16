package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.TokenRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.service.PermissionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

    private final Logger log = LoggerFactory.getLogger(TokenAuthenticationServiceImpl.class);

    private final DeviceRepository deviceRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenHasher hasher;
    private final PermissionsService permsService;

    public TokenAuthenticationServiceImpl(DeviceRepository deviceRepo, UserRepository userRepo,
                                          TokenRepository tokenRepo, TokenHasher hasher,
                                          PermissionsService permissionsService) {
        this.deviceRepo = deviceRepo;
        this.userRepo = userRepo;
        this.hasher = hasher;
        this.tokenRepo = tokenRepo;
        this.permsService = permissionsService;
    }

    @Transactional
    @Cacheable(value = "tokenAuthentication", key = "#auth.credentials")
    public Authentication authenticate(TokenAuthentication auth) {
        String tokenHash = hasher.hash(auth.getCredentials());
        Timestamp now = Timestamp.from(Instant.now());
        AccessToken activeToken = tokenRepo.findById(tokenHash)
                .filter(token -> now.before(token.getExpires()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        Optional<Device> attachedDevice = deviceRepo.findByToken(activeToken);
        if (attachedDevice.isPresent()) {
            Device device = attachedDevice.get();
            device.setLastLogin(now);
            log.debug("logged in as device {}", device.getName());
            return new TokenAuthentication(device.getName(), device.getDescription(),
                    permsService.devicePermissions(device.getUuid()), true);
        } else {
            User attachedUser = userRepo.findByToken(activeToken)
                    .orElseThrow(() -> new BadCredentialsException("bad credentials"));
            attachedUser.setLastLogin(now);
            log.debug("logged in as user {}", attachedUser.getUsername());
            return new TokenAuthentication(attachedUser.getUsername(), attachedUser.getDescription(),
                    permsService.userPermissions(attachedUser.getUuid()), false);
        }
    }
}
