package com.github.cimsbioko.server.controller.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.github.cimsbioko.server.controller.security.ExtendedUser;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.domain.model.Privilege;
import com.github.cimsbioko.server.domain.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Simple bean that provides access to the current logged in user
 * <p>
 * Reasoning behind this class can be found at:
 * http://forum.springsource.org/showthread.php?t=49686
 *
 * @author Dave
 */
public class CurrentUserImpl implements CurrentUser {

    public void setProxyUser(String username, String password, String... privileges) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public User getCurrentUser() {
        return getExtendedUser().getDomainUser();
    }

    public Set<Privilege> getCurrentUserPrivileges() {
        return getExtendedUser().getAllPrivileges();
    }

    /**
     * @return the user name of the current logged in user
     */
    public String getName() {
        return getSpringSecurityUser().getUsername();
    }

    private ExtendedUser getExtendedUser() {
        return (ExtendedUser) getSpringSecurityUser();
    }

    private UserDetails getSpringSecurityUser() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}