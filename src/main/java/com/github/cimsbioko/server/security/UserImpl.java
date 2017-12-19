package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.domain.Privilege;
import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class UserImpl extends org.springframework.security.core.userdetails.User implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    public UserImpl(User user) {
        super(user.getUsername(), user.getPassword(), convertAuthorities(user.getRoles()));
    }

    private static Collection<GrantedAuthority> convertAuthorities(Set<Role> roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getName()));
            for (Privilege privilege : role.getPrivileges()) {
                authorities.add(new SimpleGrantedAuthority(privilege.getPrivilege()));
            }
        }
        return authorities;
    }
}
