package com.github.cimsbioko.server.security;

import com.github.cimsbioko.server.domain.Privilege;
import com.github.cimsbioko.server.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Converts application roles and permissions to security framework compatible authorities.
 */
public class RoleMapper {

    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    private String rolePrefix;

    public RoleMapper() {
        this(DEFAULT_ROLE_PREFIX);
    }

    public RoleMapper(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public Collection<GrantedAuthority> rolesToAuthorities(Set<Role> roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(rolePrefix + role.getName()));
            for (Privilege privilege : role.getPrivileges()) {
                authorities.add(new SimpleGrantedAuthority(privilege.getPrivilege()));
            }
        }
        return authorities;
    }
}
