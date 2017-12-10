package com.github.cimsbioko.server.web.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.cimsbioko.server.domain.model.User;
import com.github.cimsbioko.server.dao.UserDao;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

/**
 * A custom logout success handler that clears saved session information for a user.
 */
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private static final String LOGOUT_PAGE = "/logout.faces";
    private final UserDao userDao;

    public LogoutSuccessHandler(UserDao userDao) {
        this.userDao = userDao;
        setDefaultTargetUrl(LOGOUT_PAGE);
    }

    @Transactional
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User u = userDao.findByUsername(user.getUsername()).get(0);
        u.setLastLogin(0);
        userDao.saveOrUpdate(u);
        super.onLogoutSuccess(request, response, authentication);
    }
}
