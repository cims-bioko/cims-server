package com.github.cimsbioko.server.controller.service;

import java.util.Set;

import com.github.cimsbioko.server.domain.model.Privilege;

public interface CurrentUser {

    /**
     * @return a set of all privileges for the current logged in user
     */
    Set<Privilege> getCurrentUserPrivileges();

    /**
     * Allows clients of this class to set a proxy or temporary user for the current
     * request
     *
     * @param username   of the proxy user
     * @param password   of the proxy user
     * @param privileges array of any privileges that this proxy user should have
     */
    void setProxyUser(String username, String password, String[] privileges);
}