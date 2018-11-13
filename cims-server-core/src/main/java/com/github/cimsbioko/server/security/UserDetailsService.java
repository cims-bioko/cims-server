package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.User;
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

    private UserRepository userDao;
    private UserMapper userMapper;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        User user = userDao.findByUsernameAndDeletedIsNull(username);
        if (user == null) { // no user found by the name
            throw new UsernameNotFoundException("user " + username + " was not found");
        }
        return userMapper.userToUserDetails(user);
    }

    public void setUserRepository(UserRepository userDao) {
        this.userDao = userDao;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}
