package com.github.cimsbioko.server.controller.exception;

import java.util.ArrayList;
import java.util.List;

public class ConstraintViolations extends Exception {

    private static final long serialVersionUID = 4392790814928552607L;

    private List<String> violations = new ArrayList<>();

    public ConstraintViolations() {
    }

    public ConstraintViolations(String msg, List<String> violations) {
        super(msg);
        this.violations = violations;
    }

    public ConstraintViolations(String msg) {
        super(msg);
        violations.add(msg);
    }

    public List<String> getViolations() {
        return violations;
    }

    public void addViolations(String violation) {
        violations.add(violation);
    }

    public boolean hasViolations() {
        return violations != null && violations.size() > 0;
    }

    public static void addViolationIfNotNull(ConstraintViolations cv, String violation) {
        if (null != cv) {
            cv.addViolations(violation);
        }
    }
}

