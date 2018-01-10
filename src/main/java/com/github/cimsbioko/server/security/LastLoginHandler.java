package com.github.cimsbioko.server.security;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.dao.UserDao;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

/**
 * A custom authentication success handler that records the last login time for a user.
 */
public class LastLoginHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String WELCOME_PAGE = "/welcome.faces";
    private final UserDao userDao;

    public LastLoginHandler(UserDao userDao) {
        this.userDao = userDao;
        setDefaultTargetUrl(WELCOME_PAGE);
    }

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User u = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User user = userDao.findByUsername(u.getUsername()).get(0);
        user.setLastLogin(Timestamp.from(Instant.now()));
        userDao.saveOrUpdate(user);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
