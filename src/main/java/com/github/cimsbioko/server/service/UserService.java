package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.exception.ConstraintViolations;

import java.util.List;

public interface UserService {

    User evaluateUser(User entityItem, String password) throws ConstraintViolations;

    User checkUser(User entityItem, String password) throws ConstraintViolations;

    boolean checkValidPassword(User entityItem, String password);

    List<Role> getRoles();

    User convertAndSetRoles(User entityItem, List<String> roles);
}
