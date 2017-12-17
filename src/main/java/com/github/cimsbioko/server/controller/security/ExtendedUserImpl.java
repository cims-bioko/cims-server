package com.github.cimsbioko.server.controller.security;

import com.github.cimsbioko.server.domain.model.Privilege;
import com.github.cimsbioko.server.domain.model.Role;
import com.github.cimsbioko.server.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExtendedUserImpl extends org.springframework.security.core.userdetails.User implements ExtendedUser {

    private static final String SPRING_ROLE = "ROLE_AUTHENTICATED";

    private Set<Privilege> allPrivileges;

    public ExtendedUserImpl(User user) {
        super(user.getUsername(), user.getPassword(), convertAuthorities(user.getRoles()));
        allPrivileges = compilePrivileges(user.getRoles());
    }

    private static Collection<GrantedAuthority> convertAuthorities(Set<Role> roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(SPRING_ROLE));
        for (Role role : roles) {
            for (Privilege privilege : role.getPrivileges()) {
                authorities.add(new SimpleGrantedAuthority(privilege.getPrivilege()));
            }
        }
        return authorities;
    }

    private static Set<Privilege> compilePrivileges(Set<Role> roles) {
        Set<Privilege> privs = new HashSet<>();
        for (Role role : roles) {
            privs.addAll(role.getPrivileges());
        }
        return privs;
    }

    @Override
    public Set<Privilege> getAllPrivileges() {
        return allPrivileges;
    }
}
