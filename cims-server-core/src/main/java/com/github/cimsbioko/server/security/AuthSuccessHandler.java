package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * A custom authentication success handler that record last login time and migrates password hashes to bcrypt.
 */
public class AuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String WELCOME_PAGE = "/";
    private final UserRepository userDao;
    private final PasswordEncoder encoder;

    public AuthSuccessHandler(UserRepository userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
        setDefaultTargetUrl(WELCOME_PAGE);
    }

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User u = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User user = userDao.findByUsernameAndDeletedIsNull(u.getUsername());
        user.setLastLogin(Timestamp.from(Instant.now()));
        if (authentication instanceof UsernamePasswordAuthenticationToken && authentication.getCredentials() != null) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
            user.setPassword(encoder.encode((CharSequence) authToken.getCredentials()));
            authToken.eraseCredentials();
        }
        userDao.save(user);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
