package com.github.cimsbioko.server.controller.exception;

public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 1446297115153803704L;

    public AuthorizationException(String message) {
        super(message);
    }
}