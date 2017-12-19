package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Role;
import com.github.cimsbioko.server.domain.model.User;

public interface UserService {

    User evaluateUser(User entityItem, String password) throws ConstraintViolations;

    User checkUser(User entityItem, String password) throws ConstraintViolations;

    boolean checkValidPassword(User entityItem, String password);

    List<Role> getRoles();

    User convertAndSetRoles(User entityItem, List<String> roles);
}
