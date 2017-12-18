package com.github.cimsbioko.server.controller.service;

public interface CurrentUser {

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