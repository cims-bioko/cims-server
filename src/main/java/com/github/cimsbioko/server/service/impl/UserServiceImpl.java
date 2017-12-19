package com.github.cimsbioko.server.service.impl;

import java.util.List;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Role;
import com.github.cimsbioko.server.domain.model.User;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.UserService;

public class UserServiceImpl implements UserService {

    private GenericDao genericDao;

    public UserServiceImpl(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    public User evaluateUser(User entityItem, String password) throws ConstraintViolations {

        if (!checkDuplicateUsername(entityItem.getUsername()))
            throw new ConstraintViolations("The username specified already exists.");
        if (!checkValidPassword(entityItem, password))
            throw new ConstraintViolations("The passwords entered do not match.");
        return entityItem;
    }

    public User checkUser(User entityItem, String password) throws ConstraintViolations {

        if (!checkValidPassword(entityItem, password))
            throw new ConstraintViolations("The passwords entered do not match.");

        return entityItem;
    }

    /**
     * Converts a string representation of the Roles for the User
     * into Roles of which the User belongs.
     */
    public User convertAndSetRoles(User entityItem, List<String> roles) {

        for (String role : roles) {
            Role r = genericDao.findByProperty(Role.class, "name", role);
            entityItem.getRoles().add(r);
        }
        return entityItem;
    }

    /**
     * Checks if the passwords match.
     */
    public boolean checkValidPassword(User entityItem, String password) {
        return entityItem.getPassword().equals(password);
    }

    /**
     * Checks if a duplicate username exists.
     */
    public boolean checkDuplicateUsername(String username) {

        List<User> list = genericDao.findListByProperty(User.class, "username", username);
        return list.size() <= 0;
    }

    public List<Role> getRoles() {
        return genericDao.findAllDistinct(Role.class);
    }
}
