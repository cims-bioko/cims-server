package com.github.cimsbioko.server.controller.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.github.cimsbioko.server.controller.security.ExtendedUser;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.domain.model.Privilege;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.unmodifiableSet;

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
        getSecurityContext().setAuthentication(auth);
    }

    public Set<Privilege> getCurrentUserPrivileges() {
        return isAuthenticated()? getExtendedUser().getAllPrivileges() : unmodifiableSet(EMPTY_SET);
    }

    /**
     * @return the user name of the current logged in user
     */
    public String getName() {
        return isAuthenticated()? getSpringSecurityUser().getUsername() : null;
    }

    private ExtendedUser getExtendedUser() {
        return (ExtendedUser) getSpringSecurityUser();
    }

    private SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    private Authentication getAuthentication() {
        return getSecurityContext().getAuthentication();
    }

    private boolean isAuthenticated() {
        return getAuthentication() != null;
    }

    private UserDetails getSpringSecurityUser() {
        return isAuthenticated() ? (UserDetails) getAuthentication().getPrincipal() : null;
    }

    @Override
    public String toString() {
        return getName();
    }
}