package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckFieldNotBlank;

public class CheckFieldNotBlankImpl implements ConstraintValidator<CheckFieldNotBlank, String> {

    public void initialize(CheckFieldNotBlank annotation) {
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return value.trim().length() != 0;
    }

}
