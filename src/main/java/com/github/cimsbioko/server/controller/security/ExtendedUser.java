package com.github.cimsbioko.server.controller.security;

import com.github.cimsbioko.server.domain.model.Privilege;
import com.github.cimsbioko.server.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

public interface ExtendedUser extends UserDetails {
    User getDomainUser();
    Set<Privilege> getAllPrivileges();
}
