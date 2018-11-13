package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.domain.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Converts application users to security framework compatible users.
 */
public class UserMapper {

    private RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserDetails userToUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), roleMapper.rolesToAuthorities(user.getRoles())
        );
    }
}
