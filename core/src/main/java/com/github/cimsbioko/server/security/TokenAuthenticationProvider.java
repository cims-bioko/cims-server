package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.TokenRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class TokenAuthenticationProvider implements AuthenticationProvider {

    private final Logger log = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

    private final DeviceRepository deviceRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final RoleMapper roleMapper;
    private final TokenHasher hasher;

    public TokenAuthenticationProvider(DeviceRepository deviceRepo, UserRepository userRepo, TokenRepository tokenRepo, RoleMapper roleMapper, TokenHasher hasher) {
        this.deviceRepo = deviceRepo;
        this.userRepo = userRepo;
        this.roleMapper = roleMapper;
        this.hasher = hasher;
        this.tokenRepo = tokenRepo;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        Timestamp now = Timestamp.from(Instant.now());
        TokenAuthentication auth = (TokenAuthentication) authentication;
        String tokenHash = hasher.hash(auth.getCredentials());
        AccessToken activeToken = tokenRepo.findById(tokenHash)
                .filter(token -> now.before(token.getExpires()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        Optional<Device> attachedDevice = deviceRepo.findByToken(activeToken);
        if (attachedDevice.isPresent()) {
            Device device = attachedDevice.get();
            device.setLastLogin(now);
            log.debug("logged in as device {}", device.getName());
            return new TokenAuthentication(device.getName(), device.getDescription(), roleMapper.rolesToAuthorities(device.getRoles()));
        } else {
            User attachedUser = userRepo.findByToken(activeToken)
                    .orElseThrow(() -> new BadCredentialsException("bad credentials"));
            attachedUser.setLastLogin(now);
            log.debug("logged in as user {}", attachedUser.getUsername());
            return new TokenAuthentication(attachedUser.getUsername(), attachedUser.getDescription(), roleMapper.rolesToAuthorities(attachedUser.getRoles()));
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}
