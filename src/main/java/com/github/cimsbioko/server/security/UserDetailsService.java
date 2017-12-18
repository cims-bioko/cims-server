package com.github.cimsbioko.server.security;

import java.util.List;

import com.github.cimsbioko.server.dao.UserDao;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's {@link org.springframework.security.core.userdetails.UserDetailsService}
 * Looks up a user based on username and returns a {@link UserDetails} instance that is
 * used by Spring Security
 *
 * @author Dave Roberge
 */
@Transactional
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private UserDao userDao;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        List<com.github.cimsbioko.server.domain.model.User> users = userDao.findByUsername(username);
        if (users == null || users.size() == 0) { // no user found by the name
            throw new UsernameNotFoundException("user " + username + " was not found");
        }
        return new UserImpl(users.get(0));
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
